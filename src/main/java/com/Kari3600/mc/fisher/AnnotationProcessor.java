package com.Kari3600.mc.fisher;

import com.Kari3600.mc.fisher.bukkit.BukkitProvider;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.Kari3600.mc.fisher.AutoListener","com.Kari3600.mc.fisher.AutoCommand"})
public class AnnotationProcessor extends AbstractProcessor {

    private static final Map<String,String> configSimpleTypes = new HashMap<String,String>() {{
        put("java.lang.String","config.getString($S)");
        put("int","config.getInt($S)");
        put("java.lang.Integer","config.getInt($S)");
        put("boolean","config.getBoolean($S)");
        put("java.lang.Boolean","config.getBoolean($S)");
        put("double","config.getDouble($S)");
        put("java.lang.Double","config.getDouble($S)");
        put("long","config.getLong($S)");
        put("java.lang.Long","config.getLong($S)");
        put("org.bukkit.util.Vector","config.getVector($S)");
    }};

    private static final Map<String,String> configCollectionTypes = new HashMap<String,String>() {{
        put("java.util.List","($T) config.getList($S)");
        put("java.util.Collection","($T) config.getList($S)");
        put("java.util.Set","($T) new HashSet<>(config.getList($S))");
    }};

    private String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (annotations.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "No annotations found");
            return false;
        }
        Set<? extends Element> plugins = roundEnvironment.getElementsAnnotatedWith(AutoPlugin.class);
        if (plugins.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "No plugins found");
            return false;
        }

        Types typeUtils = processingEnv.getTypeUtils();
        Elements elementUtils = processingEnv.getElementUtils();

        if (plugins.size() > 1) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Only one @Plugin class is allowed.");
            return true;
        }

        TypeElement pluginElement = (TypeElement) plugins.iterator().next();

        String packageName = processingEnv.getElementUtils()
                .getPackageOf(pluginElement)
                .getQualifiedName()
                .toString();

        // find Listeners
        Set<TypeElement> listeners = (Set<TypeElement>) roundEnvironment.getElementsAnnotatedWith(AutoListener.class);

        // find Commands
        Set<TypeElement> commands = (Set<TypeElement>) roundEnvironment.getElementsAnnotatedWith(AutoCommand.class);

        Set<TypeElement> serializables = (Set<TypeElement>) roundEnvironment.getElementsAnnotatedWith(AutoSerializable.class);

        Set<ExecutableElement> configurationMethods = (Set<ExecutableElement>) roundEnvironment.getElementsAnnotatedWith(AutoConfiguration.class);

        // build GeneratedPlugin class
        ClassName javaPlugin = ClassName.get("org.bukkit.plugin.java", "JavaPlugin");
        ClassName bukkit = ClassName.get("org.bukkit", "Bukkit");
        ClassName configurationSerialization = ClassName.get("org.bukkit.configuration.serialization", "ConfigurationSerialization");
        ClassName fileConfiguration = ClassName.get("org.bukkit.configuration.file", "FileConfiguration");

        FieldSpec delegate = FieldSpec.builder(ClassName.get(pluginElement), "delegate",
                        Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T(this)", ClassName.get(pluginElement))
                .build();

        FieldSpec fishContainer = FieldSpec.builder(FishContainer.class, "fishContainer",
                Modifier.PRIVATE, Modifier.FINAL)
                .initializer("$T.get()", FishContainer.class)
                .build();

        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("fishContainer.register(delegate)");


        // onEnable method
        MethodSpec.Builder onEnable = MethodSpec.methodBuilder("onEnable")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);

        onEnable.addStatement("reloadConfig()");

        // register listeners
        for (TypeElement e : listeners) {
            onEnable.addStatement("$T.getPluginManager().registerEvents(fishContainer.getFish($T.class), this)",
                    bukkit, ClassName.get(e));
        }

        for (TypeElement e : commands) {
            AutoCommand commandAnnotation = e.getAnnotation(AutoCommand.class);
            onEnable.addStatement("getCommand($S).setExecutor(fishContainer.getFish($T.class))",
                    commandAnnotation.name(),ClassName.get(e));
        }

        onEnable.addStatement("delegate.onEnable()");

        // onDisable method
        MethodSpec.Builder onDisable = MethodSpec.methodBuilder("onDisable")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);

        onDisable.addStatement("delegate.onDisable()");

        // onLoad method
        MethodSpec.Builder onLoad = MethodSpec.methodBuilder("onLoad")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);

        for (TypeElement e : serializables) {
            onLoad.addStatement("$T.registerClass($T.class)", configurationSerialization, e.asType());
        }

        onLoad.addStatement("delegate.onLoad()");

        MethodSpec.Builder reloadConfig = MethodSpec.methodBuilder("reloadConfig")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);

        reloadConfig.addStatement("super.reloadConfig()");
        reloadConfig.addStatement("$T config = getConfig()",fileConfiguration);
        TypeMirror serializableType = elementUtils.getTypeElement("org.bukkit.configuration.serialization.ConfigurationSerializable").asType();
        for (ExecutableElement e : configurationMethods) {
            AutoConfiguration annotation = e.getAnnotation(AutoConfiguration.class);
            TypeMirror parameterType = e.getParameters().get(0).asType();
            if (configSimpleTypes.containsKey(parameterType.toString())) {
                reloadConfig.addStatement((e.getModifiers().contains(Modifier.STATIC) ? "$T" : "fishContainer.getFish($T.class)") + ".$L(" + configSimpleTypes.get(parameterType.toString()) + ")",
                        e.getEnclosingElement(),
                        e.getSimpleName().toString(),
                        annotation.path()
                );
            } else if (configCollectionTypes.containsKey(typeUtils.erasure(parameterType).toString())) {
                reloadConfig.addStatement((e.getModifiers().contains(Modifier.STATIC) ? "$T" : "fishContainer.getFish($T.class)") + ".$L(" + configCollectionTypes.get(typeUtils.erasure(parameterType).toString()) + ")",
                        e.getEnclosingElement(),
                        e.getSimpleName().toString(),
                        parameterType,
                        annotation.path()
                );
            } else if (typeUtils.isAssignable(parameterType, serializableType)) {
                reloadConfig.addStatement((e.getModifiers().contains(Modifier.STATIC) ? "$T" : "fishContainer.getFish($T.class)") + ".$L(($T) config.get($S))",
                        e.getEnclosingElement(),
                        e.getSimpleName().toString(),
                        parameterType,
                        annotation.path()
                );
            } else {
                throw new RuntimeException("Unsupported parameter type: " + parameterType.toString());
            }
        }

        // build class
        TypeSpec generated = TypeSpec.classBuilder("Generated"+pluginElement.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .superclass(javaPlugin)
                .addField(delegate)
                .addField(fishContainer)
                .addMethod(constructor.build())
                .addMethod(onLoad.build())
                .addMethod(onEnable.build())
                .addMethod(onDisable.build())
                .addMethod(reloadConfig.build())
                .build();

        // write file
        try {
            JavaFile.builder(packageName, generated)
                    .build()
                    .writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to write GeneratedPlugin: " + e.getMessage());
        }

        AutoPlugin pluginAnnotation = pluginElement.getAnnotation(AutoPlugin.class);

        BukkitProvider.generateSources(this, pluginAnnotation, commands.stream().map(e -> e.getAnnotation(AutoCommand.class)).collect(Collectors.toList()), configurationMethods.stream().map(e -> e.getAnnotation(AutoConfiguration.class)).collect(Collectors.toList()), packageName + ".Generated" + pluginElement.getSimpleName().toString() );

        return true;
    }

    public Writer generateFile(String packageName, String fileName) {
        try {
            return processingEnv.getFiler()
                    .createResource(StandardLocation.CLASS_OUTPUT, packageName, fileName)
                    .openWriter();
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate "+fileName+": " + e.getMessage());
            return null;
        }
    }
}
