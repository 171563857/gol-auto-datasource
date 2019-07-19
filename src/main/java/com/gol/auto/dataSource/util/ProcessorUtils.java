package com.gol.auto.dataSource.util;

import com.gol.auto.dataSource.entity.Auto;
import com.gol.auto.dataSource.entity.Config;
import com.gol.auto.dataSource.entity.DataBase;
import com.squareup.javapoet.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.sql.DataSource;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessorUtils {

    public static FileObject getAutoDataSourceYml(Filer filer, String country) throws Exception {
        FileObject yml = filer.getResource(StandardLocation.CLASS_OUTPUT, "", "autoDataSource.yml");
//        if (yml.getLastModified() == 0) {
//            String error = country.equals("CN")
//                    ? "在resources目录下未找到autoDataSource.yml 配置文件!!!"
//                    : "did not found autoDataSource.yml in resources folder !!!";
//            throw new Exception(error);
//        }
        return yml;
    }

    public static Auto analysis(FileObject yml, String country) throws Exception {
        try (InputStream is = yml.openInputStream()) {
            Yaml yaml = new Yaml(new Constructor(Config.class));
            Config config = yaml.loadAs(is, Config.class);
            if (null == config || null == config.getGol() || null == config.getGol().getAuto()) {
                String error = country.equals("CN")
                        ? "解析autoDataSource.yml 配置文件失败 !!!"
                        : "Analyzing autoDataSource.yml fail !!!";
                throw new Exception(error);
            }
            return config.getGol().getAuto();
        } catch (Exception e) {
            throw e;
        }
    }

    public static void generateJavaFile(Filer filer, Auto config, String country) throws Exception {
        AtomicBoolean first = new AtomicBoolean(true);

        for (DataBase database : config.getDatabases()) {
            if (StringUtils.isEmpty(database.getSchema()) || StringUtils.isEmpty(database.getMapperPackages())) {
                throw new Exception("schema属性或mapperPackage属性缺失!!!");
            }
            String name = database.getMapperPackages().substring(0, 1).toUpperCase() + database.getMapperPackages().substring(1);
            String[] basePackages = database.getMapperPackages().split(",");
            for (int i = 0; i < basePackages.length; i++) {
                String bp = basePackages[i];
                basePackages[i] = String.format("%s.%s", config.getBasePackages(), bp.replaceAll(" ", ""));
            }
            // 生成datasource文件
            dataSource(database, first, filer, config, name, basePackages);
            // 生成MapperScannerConfigurer
            first.set(false);
        }
    }

    private static void dataSource(DataBase database, AtomicBoolean first, Filer filer, Auto config, String name,
                                   String[] basePackages) throws Exception {
        // 字符串拼接
        String factoryBeanName = String.format("%sSqlSessionFactory", database.getMapperPackages());
        String templateBeanName = String.format("%sSqlSessionTemplate", database.getMapperPackages());
        String dataSourceBeanName = String.format("%sDataSource", database.getMapperPackages());
//        String factoryBeanName = "sqlSessionFactory";
//        String templateBeanName = "sqlSessionTemplate";
//        String dataSourceBeanName = "dataSource";
        // 注解
        List<AnnotationSpec> annotationSpecs = new ArrayList<>();
        AnnotationSpec configuration = AnnotationSpec.builder(Configuration.class).build();
        annotationSpecs.add(configuration);
        AnnotationSpec mapperScan = AnnotationSpec.builder(MapperScan.class)
                .addMember("basePackages", "$S", basePackages)
//                .addMember("sqlSessionFactoryRef", "$S", factoryBeanName)
                .addMember("sqlSessionTemplateRef", "$S", templateBeanName)
                .build();
        annotationSpecs.add(mapperScan);
        // 注入 EnvironmentUtil
//        FieldSpec environmentUtil = FieldSpec.builder(EnvironmentUtil.class, "environmentUtil", Modifier.PRIVATE)
//                .addAnnotation(Autowired.class).build();
        // propertyResolver
        FieldSpec propertyResolver = FieldSpec.builder(RelaxedPropertyResolver.class, "propertyResolver", Modifier.PRIVATE).build();
        // 方法列表
        List<MethodSpec> methodSpecs = new ArrayList<>();
        // 实现 EnvironmentAware
        MethodSpec.Builder setEnvironment = MethodSpec.methodBuilder("setEnvironment")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.VOID)
                .addParameter(Environment.class, "environment")
                .addStatement("this.propertyResolver = new $T(environment, null)", RelaxedPropertyResolver.class);
        methodSpecs.add(setEnvironment.build());
        // DataSource
        methodSpecs.add(dataSource(database.getSchema(), dataSourceBeanName, first, config));
        // SqlSessionFactory
//        methodSpecs.add(sqlSessionFactory(database.getMapperPackages(), factoryBeanName, dataSourceBeanName, first));
        // SqlSessionTemplate
//        methodSpecs.add(sqlSessionTemplateByFactory(templateBeanName, factoryBeanName, first));
        methodSpecs.add(sqlSessionTemplateByDataSource(templateBeanName, dataSourceBeanName, database.getMapperPackages(), first));
        TypeSpec finderClass = TypeSpec.classBuilder(name + "DataSourceConfig")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotations(annotationSpecs)
                .addSuperinterface(ParameterizedTypeName.get(EnvironmentAware.class))
//                .addField(environmentUtil)
                .addField(propertyResolver)
                .addMethods(methodSpecs)
                .build();

//        com.gol.auto.dataSource.configuration
        JavaFile javaFile = JavaFile.builder("cn.gol.configuration", finderClass)
                .build();
        javaFile.writeTo(filer);
    }

    private static void mapperScannerConfigurer(DataBase database, AtomicBoolean first, Filer filer, Auto config,
                                                String name, String basePackages) {
        // 字符串拼接
        String mapperBeanName = String.format("%sMapperScannerConfigurer", database.getMapperPackages());
        // 注解
        List<AnnotationSpec> annotationSpecs = new ArrayList<>();
        AnnotationSpec configuration = AnnotationSpec.builder(Configuration.class).build();
        annotationSpecs.add(configuration);
    }

    private static MethodSpec dataSource(String schema, String dataSourceBeanName, AtomicBoolean first, Auto config) {
        AnnotationSpec bean = AnnotationSpec.builder(Bean.class)
                .addMember("name", "$S", dataSourceBeanName).build();
        MethodSpec.Builder builder = MethodSpec.methodBuilder("dataSource")
                .addModifiers(Modifier.PUBLIC)
                .returns(DataSource.class)
                .addAnnotation(bean);
//                .addStatement("$T propertyResolver = environmentUtil.propertyResolver()", RelaxedPropertyResolver.class);
        if (config.isJtaTransactionManager()) {
            builder.addStatement("return $T.atomikos(this.propertyResolver, $S)", DataSourceUtils.class, schema);
        } else {
            builder.addStatement("return $T.druid(this.propertyResolver, $S)", DataSourceUtils.class, schema);
        }
        if (first.get()) {
            builder.addAnnotation(Primary.class);
        }
        return builder.build();
    }

    private static MethodSpec sqlSessionFactory(String mapperPackage, String factoryBeanName, String dataSourceBeanName,
                                                AtomicBoolean first) {
        AnnotationSpec bean = AnnotationSpec.builder(Bean.class)
                .addMember("name", "$S", factoryBeanName).build();
        AnnotationSpec qualifier = AnnotationSpec.builder(Qualifier.class)
                .addMember("value", "$S", dataSourceBeanName).build();
        ParameterSpec parameterSpec = ParameterSpec.builder(DataSource.class, "dataSource")
                .addAnnotation(qualifier)
                .build();
        String basePackages = String.format("classpath:mapper/%s/*.xml", mapperPackage);
        MethodSpec.Builder builder = MethodSpec.methodBuilder("sqlSessionFactory")
                .addAnnotation(bean)
                .addModifiers(Modifier.PUBLIC)
                .returns(SqlSessionFactory.class)
                .addParameter(parameterSpec)
                .addStatement("return $T.sqlSessionFactory(dataSource, $S)", DataSourceUtils.class, basePackages);
        if (first.get()) {
            builder.addAnnotation(Primary.class);
        }
        return builder.build();
    }

    private static MethodSpec sqlSessionTemplateByFactory(String templateBeanName, String factoryBeanName,
                                                          AtomicBoolean first) {
        AnnotationSpec bean = AnnotationSpec.builder(Bean.class)
                .addMember("name", "$S", templateBeanName).build();
        AnnotationSpec qualifier = AnnotationSpec.builder(Qualifier.class)
                .addMember("value", "$S", factoryBeanName).build();
        ParameterSpec parameterSpec = ParameterSpec.builder(SqlSessionFactory.class, "sqlSessionFactory")
                .addAnnotation(qualifier)
                .build();
        MethodSpec.Builder builder = MethodSpec.methodBuilder("sqlSessionTemplate")
                .addAnnotation(bean)
                .addModifiers(Modifier.PUBLIC)
                .returns(SqlSessionTemplate.class)
                .addParameter(parameterSpec)
                .addStatement("return new $T(sqlSessionFactory)", SqlSessionTemplate.class);
        if (first.get()) {
            builder.addAnnotation(Primary.class);
        }
        return builder.build();
    }

    private static MethodSpec sqlSessionTemplateByDataSource(String templateBeanName, String dataSourceBeanName,
                                                             String mapperPackage, AtomicBoolean first) {
        AnnotationSpec bean = AnnotationSpec.builder(Bean.class)
                .addMember("name", "$S", templateBeanName).build();
        AnnotationSpec qualifier = AnnotationSpec.builder(Qualifier.class)
                .addMember("value", "$S", dataSourceBeanName).build();
        ParameterSpec parameterSpec = ParameterSpec.builder(DataSource.class, "dataSource")
                .addAnnotation(qualifier)
                .build();
        String basePackages = String.format("classpath:mapper/%s/*.xml", mapperPackage);
        MethodSpec.Builder builder = MethodSpec.methodBuilder("sqlSessionTemplate")
                .addAnnotation(bean)
                .addModifiers(Modifier.PUBLIC)
                .returns(SqlSessionTemplate.class)
                .addParameter(parameterSpec)
                .addStatement("return new $T(DataSourceUtils.sqlSessionFactory(dataSource, $S))", SqlSessionTemplate.class, basePackages);
        if (first.get()) {
            builder.addAnnotation(Primary.class);
        }
        return builder.build();
    }
}
