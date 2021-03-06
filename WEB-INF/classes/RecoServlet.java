import java.util.*;
import java.io.IOException;  
import java.io.PrintWriter;  
import javax.servlet.ServletException;  
import javax.servlet.http.Cookie;  
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpSession;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;
// Imports need to be checked
import java.sql.*;  
/* Some Documentation:
        Errors through Cookies: 
                1) recofailed*/

public class RecoServlet extends HttpServlet 
{  
    protected void doPost(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException 
    {
       
        response.setContentType("text/html");  
        System.out.println("In RecoServlet");
        String cType = request.getParameter("cType");  
        String titles="";
        String confidence="";
        String errorMsg = null;        
          // JDBC driver name and database URL
        String JDBC_DRIVER="oracle.jdbc.driver.OracleDriver";  
        String DB_URL="jdbc:oracle:thin:@localhost:1521:XE"; 
           //  Database credentials
        String USER = "system";
        String PASS = "2710";   //JAWAHAR: You will have to change this for your own database during testing
        PreparedStatement ps = null;
        ResultSet rs = null;
        PreparedStatement ps_check = null;
        ResultSet rs_check = null;
          //Checking if the user is already logged in using the Cookie
        Cookie[] cookies= request.getCookies();
        String name=null;
        for (Cookie ck: cookies) {
            if("username".equals(ck.getName()))
                name=ck.getValue();
        }
        System.out.println("User : " + name);
        if(name==null) {
            errorMsg="No User Logged In!";  //changed to use this directly as the error message
            Cookie cookey = new Cookie("recofailed", errorMsg);
            cookey.setMaxAge(60*2); 
            response.addCookie(cookey);
            response.sendRedirect(request.getContextPath()+"/index.html");
        }
        if(name.compareTo("kinkax")==0) {
            for(int j=0;j<10;j++)
               {
               	 titles+= String.valueOf(100-j)+"|";
               	 confidence+=String.valueOf(100-(2*j))+"|";
               } //data for poor kinkax, without a database
            System.out.println("Exiting 'Kinkax'");
            Cookie cookey1 = new Cookie("titles", titles);
            cookey1.setMaxAge(60*25);       //25 mins
            response.addCookie(cookey1); 
            Cookie cookey2 = new Cookie("confidence", confidence);
            cookey2.setMaxAge(60*25); 
            response.addCookie(cookey2);
            Cookie cookey3 = new Cookie("cType",cType);
            response.addCookie(cookey3);
            response.sendRedirect(request.getContextPath()+"/results.html");
            return;
        }
       
         
        try {
         
            Class.forName(JDBC_DRIVER);
            //Open the Connection
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            //Preparing Query
            if (conn != null) {
                System.out.println("Connected");
                                  
               /* ps_check=conn.prepareStatement("select count(*) as count_val from user_"+cType+" where user_id ='"+name+"'");
                rs_check= ps_check.executeQuery();
                int count_val=rs_check.getInt("count_val");
	             if(count_val<1)
	                {
	               		errorMsg="User has mentioned no previous Preferences!";  //changed to use this directly as the error message
                    	Cookie cookey = new Cookie("recofailed", errorMsg);	
                   		 cookey.setMaxAge(60*2); 
                   		 response.addCookie(cookey);
                   		 response.sendRedirect(request.getContextPath()+"/"+cType+".html"); 	
	                }*/
                    System.out.println("cType: "+cType);
                ps = conn.prepareStatement("with c_user as (select "+cType+"_id,rating from user_"+cType+" where user_id='"+name+"'),ranks as (select um.user_id,x."+cType+"_id,x.rating*um.rating as rank from c_user x, user_"+cType+" um where um."+cType+"_id= x."+cType+"_id),bonds as (select user_id, sum(rank) as bond from ranks group by user_id), r_pool as (select "+cType+"_id, rating*bond as rating007 from user_"+cType+" natural join bonds where "+cType+"_id not in (select "+cType+"_id from c_user)),d_pool as (select "+cType+"_id,sum(rating007) as final_score from r_pool group by "+cType+"_id) select title,final_score from d_pool natural join "+cType+" order by final_score desc");
                rs = ps.executeQuery();
                System.out.println("After Query Execution"); 
                if(!rs.next()) 
                { 
                    errorMsg="User has mentioned no previous Preferences!";  //changed to use this directly as the error message
                    Cookie cookey = new Cookie("recofailed", errorMsg);
                    cookey.setMaxAge(60*2); 
                    response.addCookie(cookey);
                    response.sendRedirect(request.getContextPath()+"/"+cType+".html");
                }
                Long final_score[]= new Long[10];
                
                int i=0;
               while(rs.next() & (i<10))
                {
 /*titles here->*/  titles+=rs.getString("title")+"|";
                    final_score[i]= (rs.getLong("final_score"));
                    i++;
                }
                Long min,max;
                //min=max=final_score[0];
                //i=0;
                int j =0;
                while((j<10)&&(final_score[j]!=null)) j++;
                System.out.println("Number of final results : "+j);
                max=final_score[0];
                System.out.println("Highest Score : "+max);
                min=final_score[9];
                System.out.println("\"Least Score\" : " +min);
                /*while(i<10)
                {
                    if(final_score[i]>max)max=final_score[i];
                    if(final_score[i]<min)min=final_score[i];
                    i++;
                }*/
                i=0;
                int range = (int)(max-min);
                
                while(i<10)
                {
                    int temp= (int)((final_score[i]-min)*20)/range;
                    final_score[i] = (long)(80 + temp);
                    i++;
                }
                
                i=0;
                while(i<10)
                {
                    confidence+=String.valueOf(final_score[i])+"|";
                	i++;
                }

            }
            else {  //if connection is null
                System.out.println("Couldn't connect to Database");
            }
            /*Sending two Strings:
            						1) confidence
									2) titles
            */
				
				 Cookie cookey1 = new Cookie("titles", titles);
            cookey1.setMaxAge(60);
            response.addCookie(cookey1); 
            Cookie cookey2 = new Cookie("confidence", confidence);
            cookey2.setMaxAge(60); 
            response.addCookie(cookey2);
            Cookie cookey3 = new Cookie("cType",cType);
            //cookey3.setMaxAge(10*60); 
            response.addCookie(cookey3);
           // request.getSession(true).setAttribute("titles",titles);
            //request.getSession(true).setAttribute("confidence",confidence);
            
            response.sendRedirect(request.getContextPath()+"/results.html");

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        finally 
        {
            try
            {
                if(rs!=null) rs.close();
                if(ps!=null) ps.close();
                if(rs_check!=null) rs_check.close();
                if(ps_check!=null) ps_check.close();
            }catch(Exception e){e.printStackTrace();}
        } 
    }
}
