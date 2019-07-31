package com.gol.auto.dataSource.processor;

import com.gol.auto.dataSource.entity.Auto;
import com.gol.auto.dataSource.util.ProcessorUtils;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

@SupportedAnnotationTypes("*")
@SupportedOptions("debug")
@AutoService(Processor.class)
public class DataSourceProcessor extends AbstractProcessor {
    private static boolean alreadyCreate = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (alreadyCreate) {
            return false;
        }
        // 获取配置文件
        Filer filer = processingEnv.getFiler();
        String country = processingEnv.getLocale().getCountry();
        try {
            // 获取autoDataSource.yml配置文件流
            FileObject yml = ProcessorUtils.getAutoDataSourceYml(processingEnv, filer, country);
            if (null == yml) {
                return false;
            }
            // 解析autoDataSource.yml配置文件
            Auto config = ProcessorUtils.analysis(yml, country);
            // 生成dataSource文件
            ProcessorUtils.generateJavaFile(filer, config, country);
        } catch (Exception e) {
            fatalError(e);
        } finally {
            alreadyCreate = true;
            return false;
        }
    }

    protected void fatalError(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        fatalError(writer.toString());
    }

    protected void fatalError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }
}
