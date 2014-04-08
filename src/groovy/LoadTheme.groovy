/**
 * Created by IntelliJ IDEA.
 * User: Yiguang
 * Date: Oct 10, 2009
 * Time: 11:25:08 PM
 * To change this template use File | Settings | File Templates.
 */
//sql=groovy.sql.Sql.newInstance("jdbc:postgresql://localhost:5432/sword","postgres","light123","org.postgresql.Driver")
//sql=groovy.sql.Sql.newInstance("jdbc:postgresql://rock.ccim.org:5432/sword","postgres","light123","org.postgresql.Driver")
  def out=new File("c:/tmp/membiblesch.txt");
 def lastid=0
  new File("c:/bibleongrails/gsword/src/java/dailymem.txt").splitEachLine("—"){token->
  def thmem=token[0]
    println thmem 
  def vs=token[1]

    vs?.split(";").each { v->
      out.append(lastid++ +";"+ thmem+";"+v +" \n");
  }

}