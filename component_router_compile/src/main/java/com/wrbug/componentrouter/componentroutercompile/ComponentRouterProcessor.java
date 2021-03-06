package com.wrbug.componentrouter.componentroutercompile;

import com.google.auto.service.AutoService;
import com.wrbug.componentrouter.annotation.ObjectRoute;
import com.wrbug.componentrouter.componentroutercompile.generator.ComponentInstanceRouterFinderGenerator;
import com.wrbug.componentrouter.componentroutercompile.generator.ComponentRouterFinderGenerator;
import com.wrbug.componentrouter.componentroutercompile.generator.Generator;
import com.wrbug.componentrouter.componentroutercompile.generator.MethodRouterGenerator;
import com.wrbug.componentrouter.componentroutercompile.generator.ObjectRouterGenerator;
import com.wrbug.componentrouter.componentroutercompile.util.Log;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import java.util.*;

@AutoService(Processor.class)

public class ComponentRouterProcessor extends AbstractProcessor {
    private Log log;
    private Elements mElementUtils;
    private Filer mFiler;
    private Map<String, String> javaMethodMap = new HashMap<>();
    private String moduleName;
    private List<String> objectFinalMethodNames;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        log = new Log(processingEnv.getMessager());
        // 生成文件所需
        javaMethodMap.clear();
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        moduleName = processingEnv.getOptions().get("moduleName");
        if (moduleName != null) {
            moduleName = moduleName.replace("-", "_").toLowerCase();
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportAnnotations = new HashSet<>();
        supportAnnotations.add(ObjectRoute.class.getCanonicalName());
        log.printMessage(supportAnnotations.toString());
        return supportAnnotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //方法注册生成
        MethodRouterGenerator methiodGenerator = new MethodRouterGenerator(mFiler, log);
        //实例注册生成
        ObjectRouterGenerator objectRouterGenerator = new ObjectRouterGenerator(mFiler, log);
        Set<? extends Element> javaClassElements = roundEnvironment.getElementsAnnotatedWith(ObjectRoute.class);
        for (Element javaClassElement : javaClassElements) {
            if (javaClassElement instanceof TypeElement) {
                methiodGenerator.setElement((TypeElement) javaClassElement);
                methiodGenerator.generate();
                objectRouterGenerator.setElement((TypeElement) javaClassElement);
                objectRouterGenerator.generate();
            }
        }
        Generator generator = new ComponentRouterFinderGenerator(mFiler, javaClassElements);
        generator.generate();
        generator = new ComponentInstanceRouterFinderGenerator(mFiler, javaClassElements);
        generator.generate();
        return true;
    }
}
