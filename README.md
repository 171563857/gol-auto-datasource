# gol-auto-datasource
需要在resources目录下创建autoDataSource.yml配置文件,并配置相关参数
gol:
  auto:
    # 是否使用PageHelper插件,默认使用
    pageHelper: true
    # 是否使用Jta事务管理器,默认不使用Atomikos
    jtaTransactionManager: false
    # 数据源类型
    dataSourceClassName: druid
    # 接口地址
    basePackages: cn.gol.mapper
    # 生成class文件地址,默认cn.gol.configuration
    configPath: cn.gol.configuration
    # 多源数据库前缀,默认jdbc,
    # 例如: jdbc.[数据库库名'user'].[连接地址'url'], jdbc.[数据库库名'user'].[用户名'username'], jdbc.[数据库库名'user'].[用户名'密码']
    # jdbc.user.url; jdbc.user.username; jdbc.user.password
    # 遍历databases.schema属性进行代码生成
    dbPrefix: jdbc
    databases:
      # 数据库库名
    - schema: user
      # xml文件地址,多个地址用","逗号隔开,例如'user',路径为resources/mapper/user
      mapperPackage: user
    - schema: book
      mapperPackage: book
