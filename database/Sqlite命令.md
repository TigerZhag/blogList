# 打开DB文件
sqlite3 xxx.db

# 数据库结构
sqlite数据库结构信息存储在sqlite_master表中，不能手动修改，只可以查看

# 特性：快捷命令
1. .tables 显示数据库中所有的表和视图：SELECT name FROM sqlite_master WHERE type IN ('table','view') AND name NOT LIKE 'sqlite_%'UNION ALL SELECT name FROM sqlite_temp_master WHERE type IN ('table','view') ORDER BY 1；

2. .schema <tablename> 查看创建表的语句，即查看表结构  SELECT sql FROM (SELECT * FROM sqlite_master UNION ALL SELECT * FROM sqlite_temp_master)WHERE type!='meta'ORDER BY tbl_name, type DESC, name；

3. .explain 特有的调试扩展，如果任何常规的SQL被 EXPLAIN执行，那么SQL命令被分解并分析但并不执行。

# 使用心得
1. Like 关键字要少用,无论对于有没有索引的列,效率都不如 >= and < ;
2. 索引对于模糊查询无效,对于'abd%'之类的模糊查询,可用 'xxx >= abd and xxx < ade'代替;
3. 如果一个子句的操作符是BETWEEN，在SQLite中同样不能用索引进行优化，所以也要进行相应的等价转换： 如：a BETWEEN b AND c可以转换成：(a BETWEEN b AND c) AND (a>=b) AND (a<=c)。 在上面这个子句中， (a>=b) AND (a<=c)将被设为dynamic且是(a BETWEEN b AND c)的子句，那么如果BETWEEN语句已经编码，那么子句就忽略不计，如果存在可利用的index使得子句已经满足条件，那么父句则被忽略。
4.  对于单个表的单个列而言，如果都有形如T.C=expr这样的子句，并且都是用OR操作符连接起来，形如： x = expr1 OR expr2 = x OR x = expr3 此时由于对于OR，在SQLite中不能利用索引来优化，所以可以将它转换成带有IN操作符的子句：x IN(expr1,expr2,expr3)这样就可以用索引进行优化，效果很明显，但是如果在都没有索引的情况下OR语句执行效率会稍优于IN语句的效率。
5. 
