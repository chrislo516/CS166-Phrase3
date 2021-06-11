/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

// Library for checking date/time
import java.text.SimpleDateFormat;
import java.text.ParseException;
/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. < EXIT");
				
				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddDoctor(DBproject esql) {//1/1
		int doctor_ID;
		String name;
		String specialty;
		int did;
		Scanner sc = new Scanner(System.in);	
		do{
			System.out.print("--------Add Doctor-------\n");
			System.out.print("Dotor ID: ");
			try{
				doctor_ID = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e){
				System.out.println("Dotor ID must be an interger!\n");
				continue;
			}
			
		}while(true);
		System.out.print("Name: ");
		name = sc.nextLine();
		System.out.print("Specialty: ");
		specialty = sc.nextLine();
		System.out.print("Department ID: ");
		do{	
			try{
				did = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e){
				System.out.println("Department ID must be an interger!\n");
				continue;
			}
		}while(true);
	 
		try{
			String query  = "INSERT INTO Doctor (doctor_ID, name, specialty, did)\n";
			       query += "VALUES ("+String.valueOf(doctor_ID)+",\'"+name+"\',\'"+specialty+"\',"+String.valueOf(did)+");";
			esql.executeUpdate(query);
			System.out.println("\nUpdate Dotor Information: ");
			System.out.print("----Successfully Add Dotor info. as following to DataBase-----\n");
			System.out.print("Dotor ID      : "+ doctor_ID + "\n");
			System.out.print("Name          : "+ name + "\n");
			System.out.print("Specialty     : "+ specialty + "\n");
			System.out.print("Department ID : "+ did + "\n\n");
		}catch (Exception e){
			System.out.println(e.getMessage());
		}	      
	}
	public static void AddPatient(DBproject esql) {//2/2
            int patientID;
            String patientName;
            String gender;
            int age;
            String address;
            int numberOfAppointments;

            Scanner sc = new Scanner(System.in);
           
      
                 do{
                        System.out.print("--------Add Patient-------\n");
                        System.out.print("Patient ID: ");
                        try{
                                patientID = Integer.parseInt(in.readLine());
                                break;
                        }catch (Exception e){
                                System.out.println("Patient ID must be an integer!\n");
                                continue;
                        }

                }while(true);

                System.out.print("Name: ");
                patientName = sc.nextLine();
               

                do
                {
                   System.out.print("Gender: ");
                   gender = sc.nextLine();
                   if(gender.equals("M") || gender.equals("F"))
                   {
                       break;
                   }
                   else
                   {
                       System.out.println("Gender must be M(Male) or F(Female)");
                   }
                }while (true);
                System.out.print("Age: ");  

                 do{
                        try{
                                age = Integer.parseInt(in.readLine());
                                break;
                        }catch (Exception e){
                                System.out.println("Age must be an integer!");
                                continue;
                        }
                }while(true); 

                System.out.print("Address: ");
                address = sc.nextLine();
               
                System.out.print("Number of Appointments: ");

                do{
                        try{
                                numberOfAppointments = Integer.parseInt(in.readLine());
                                break;
                        }catch (Exception e){
                                System.out.println("Number of Appointments must be an integer!");
                                continue;
                        }
                }while(true);      
 
		try{
                        String query  = "INSERT INTO Patient (patient_ID, name, gtype, age, address, number_of_appts)\n";
                               query += "VALUES ("+String.valueOf(patientID)+",\'"+patientName+"\',\'"+gender+"\',"+String.valueOf(age)+ ",\'"+address+"\'," +String.valueOf(numberOfAppointments) +  ");\n";
                        esql.executeUpdate(query);
			System.out.print("\n----Successfully Add Patient info. as following to DataBase-----\n");
			System.out.print("Patient ID   		 : "+ String.valueOf(patientID) + "\n");
			System.out.print("Name          	 : "+ patientName + "\n");
			System.out.print("Gender		 : "+ gender+"\n");
			System.out.print("Age			 : "+ String.valueOf(age)+"\n");
			System.out.print("Address     	         : "+ gender + "\n");
			System.out.print("Number of Appointments : "+ String.valueOf(numberOfAppointments) + "\n\n");
                }catch (Exception e){
                        System.out.println(e.getMessage());
                }
	
               
}                    

	public static void AddAppointment(DBproject esql) {//3/3
		int appnt_ID;
		String status;
		String adate;
		String time_slot;
		ArrayList<String> status_opt = new ArrayList<String>();
		status_opt.add("PA");
		status_opt.add("AC");
		status_opt.add("AV");
		status_opt.add("WL");
		Scanner sc = new Scanner(System.in);	
		SimpleDateFormat sdf_date = new SimpleDateFormat("M/d/yyyy");
		SimpleDateFormat sdf_time = new SimpleDateFormat("H:m-H:m");
		
		do{
			System.out.print("--------Add Appointment-------\n");
			System.out.print("Appointment ID: ");
			try{
				appnt_ID = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e){
				System.out.println("Doctor ID must be an interger!");
				continue;
			}
			
		}while(true);
		
		do{
			try{	
				System.out.print("Date: ");
				adate = sc.nextLine();
				sdf_date.parse(adate);
				sdf_date.setLenient(false);
				break;
			}catch(ParseException e){
				System.out.println("Invalid date! Try the format(mm/dd/yyyy)");
			}
		}while(true);

		do{
			try{
				System.out.print("Time: ");
				time_slot = sc.nextLine();
				sdf_time.parse(time_slot);
				break;
			}catch(ParseException e){
				System.out.println("Invalid time slot! Try the format(H:m-H:m)");
				System.out.println("For example, 8:30a.m. - 10:00p.m. should be format (8:30-22:00)");
			}
		}while(true);

		do{	
			System.out.print("Status: ");
			status = sc.nextLine();
			if(status_opt.contains(status)){
				break;
			}else{
				System.out.println("Appointment Status must be : PA(Past), AC(Active), AV(Available) and WL(Waitlisted).");
				continue;
			}
		}while(true);
	 
	 	try{
			String query  = "INSERT INTO Appointment (appnt_ID, adate, time_slot, status)\n";
			       query += "VALUES ("+String.valueOf(appnt_ID)+",\'"+adate+"\',\'"+time_slot+"\',\'"+status+"\');\n";
			esql.executeUpdate(query);
			System.out.print("\n----Successfully Add Appointment info. as following to DataBase-----\n");
			System.out.print("Appointment ID: "+ String.valueOf(appnt_ID) + "\n");
			System.out.print("Date          : "+ adate + "\n");
			System.out.print("Time Slot     : "+ time_slot + "\n");
			System.out.print("Status        : "+ status + "\n\n");
		}catch (Exception e){
			System.out.println(e.getMessage());
		}	      
	}


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
            int patientID;
            int doctorID;
	    int apptID;
	    int age;
	    int tmp;
	    String patientName;
            String gender;
            String address;
	    String y_n = "Y";
            Scanner sc = new Scanner(System.in);
           
      
                do{
                        try{
				System.out.print("--------Please Input the Patient ID-------\n");
				System.out.print("Patient ID : ");
				patientID = Integer.parseInt(in.readLine());
				break;
			}catch(Exception e){
				System.out.println("Patient ID must be an interger!");
				continue;
			}
		}while(true);

		try{
			String query = "SELECT * \n FROM Patient \n WHERE patient_ID = " + patientID +";";
			List<List<String>> result = esql.executeQueryAndReturnResult(query);
			if(result.size() != 0){
			for(int i = 0 ; i < result.size() ; i++){
				System.out.println("------Found Patient------");
				System.out.print("Patient ID-------------: "+  result.get(i).get(0)+ "\n");
				System.out.print("Name-------------------: "+  result.get(i).get(1)+ "\n");
				System.out.print("Gender-----------------: "+  result.get(i).get(2)+ "\n");
				System.out.print("Age--------------------: "+  result.get(i).get(3)+ "\n");
				System.out.print("Address----------------: "+  result.get(i).get(4)+ "\n");
				System.out.print("Number of Appointments-: "+  result.get(i).get(5)+ "\n\n");
                			
			}
			do{
				System.out.println("Is the above patient detail you want to use for make appointment? (Y/N)");
				y_n = sc.nextLine();	
				if(y_n.equals("Y")||y_n.equals("N")){break;}else{System.out.println("Please Input either Y for Yes or N for No! (Case Sensitive)");continue;}
			}while(true);
			}
			if(result.size() == 0 || y_n.equals("N")){
				do_while:
				do{
					System.out.println("Do you want to add a new patient information to the DataBase? (Y/N)");
					y_n = sc.nextLine();	
					if(y_n.equals("Y")||y_n.equals("N")){break do_while;}else{System.out.println("Please Input either Y for Yes or N for No! (Case Sensitive)");continue;}
					
				}while(true);
			
				if(y_n.equals("Y")){
					try{
                        		      do{
						System.out.print("--------Add Patient-------\n");
						System.out.print("Patient ID: ");
						try{
							patientID = Integer.parseInt(in.readLine());
							break;
						}catch (Exception e){
							System.out.println("Patient ID must be an integer!\n");
							continue;
						}

						}while(true);

						System.out.print("Name: ");
						patientName = sc.nextLine(); 

						do
						{
						   System.out.print("Gender: ");
						   gender = sc.nextLine();
						   if(gender.equals("M") || gender.equals("F"))
						   {
						       break;
						   }
						   else
						   {
						       System.out.println("Gender must be M(Male) or F(Female)");
						   }
						}while (true);
						System.out.print("Age: ");  

						 do{
							try{
								age = Integer.parseInt(in.readLine());
								break;
							}catch (Exception e){
								System.out.println("Age must be an integer!");
								continue;
							}
						}while(true); 

						System.out.print("Address: ");
						address = sc.nextLine();
			    
						query  = "INSERT INTO Patient (patient_ID, name, gtype, age, address, number_of_appts)\n";
						query += "VALUES ("+String.valueOf(patientID)+",\'"+patientName+"\',\'"+gender+"\',"+String.valueOf(age)+ ",\'"+address+"\', 0);\n";
						esql.executeUpdate(query);
						System.out.print("\n----Successfully Add Patient info. as following to DataBase-----\n");
						System.out.print("Patient ID   		 : "+ String.valueOf(patientID) + "\n");
						System.out.print("Name          	 : "+ patientName + "\n");
						System.out.print("Gender		 : "+ gender+"\n");
						System.out.print("Age			 : "+ String.valueOf(age)+"\n");
						System.out.print("Address     	         : "+ gender + "\n");
						System.out.print("Number of Appointments : 0 \n\n");
                			}catch (Exception e){
                        				System.out.println(e.getMessage());
                			}
				}else{return;}
			}		
                }catch (Exception e){
                        System.out.println(e.getMessage());
                }
		do{
			do{
				try{
					System.out.print("Doctor ID: ");
					doctorID = Integer.parseInt(in.readLine());
					break;
				}catch(Exception e){
					System.out.println("Doctor ID must Be Integer!");
				}	
			}while(true);
			
			
			String query = "SELECT D.doctor_ID, D.name, A.appnt_ID, A.adate, A.time_slot, A.status, HS.name, HS.hospital_ID\n"+
				"FROM Doctor D, Appointment A, has_appointment H, Hospital HS, Department DE\n"+
				"WHERE D.doctor_ID = H.doctor_id\n"+
				"AND D.did = DE.Dept_ID\n"+ 
				"AND DE.hid = HS.hospital_ID\n"+
				"AND H.appt_id = A.appnt_ID\n"+
				"AND (A.status = 'AC' OR A.status = 'AV' OR A.status = 'WL')\n"+
				"AND D.doctor_ID = "+doctorID+"\n"+
				"GROUP BY D.doctor_ID, A.appnt_ID, HS.name, HS.hospital_ID\n"+
				"ORDER BY D.doctor_ID DESC;";
			
			try{
				List<List<String>> result = esql.executeQueryAndReturnResult(query);
				if(result.size() == 0){
					System.out.println("Cannot find ANY Appointment for Doctor ID with " + doctorID + " in DataBase!");
					return;
				}
			  	System.out.println("\n-------Result Found-------");	
				System.out.println("Doctor ID   : "+result.get(0).get(0));
				System.out.println("Doctor Name : "+result.get(0).get(1));
				for(int i = 0 ; i<result.size() ; i++){
					System.out.println("\n1.");
					System.out.println("Appointment ID   : "+result.get(i).get(2));
					System.out.println("Date             : "+result.get(i).get(3));		
					System.out.println("Time Slot        : "+result.get(i).get(4));
					System.out.println("Status           : "+result.get(i).get(5));
					System.out.println("Hosptial         : "+result.get(i).get(6)+ "("+result.get(i).get(7)+")");
				}
				System.out.println();
				do{
					System.out.println("-----Choose the following Appointment by input the corresponding number-----");
					try{
						do{
						        tmp = Integer.parseInt(in.readLine());
							if(tmp<=result.size()){
								break;
							}else{
								System.out.println("Error! Please input the corresponding number!");
							}
						}while(true);
						break;
					}catch(Exception e){continue;}
				}while(true);
				do{
					System.out.println(result.get((tmp-1)));
					if(result.get((tmp-1)).get(5).contains("AV")){
						 query = "UPDATE Appointment\n"+	
						         "SET status = \'AC\'\n WHERE appnt_ID = "+result.get(tmp-1).get(2)+";";
						try{
							esql.executeUpdate(query);
							System.out.println("----Successfully Make Appointment----");
							System.out.println("Hospital      : "+result.get(tmp-1).get(6));
							System.out.println("Patient     ID: "+patientID);
							System.out.println("Appointment ID: "+result.get(tmp-1).get(2));
							System.out.println("Status        : Active");
							break;
						}catch(Exception e){
							System.out.println(e.getMessage());
						}
					}else if(result.get(tmp-1).get(5).contains("AC"){
						 query = "UPDATE Appointment\n"+	
						         "SET status = \'WL\'\n WHERE appnt_ID = "+result.get(tmp-1).get(2)+";";
						try{
							esql.executeUpdate(query);
							System.out.println("----Successfully Make Appointment----");
							System.out.println("Hospital      : "+result.get(tmp-1).get(6));
							System.out.println("Patient     ID: "+patientID);
							System.out.println("Appointment ID: "+result.get(tmp-1).get(2));
							System.out.println("Status        : WaitListed");
							break;
						}catch(Exception e){
							System.out.println(e.getMessage());
						}						
					}break;
				}while(true);
				break;
			}catch(Exception e){
				System.out.println(e.getMessage());
			continue;}										
				
		}while(true);
		
	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5/5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
		int doctor_ID;
		String st_date_range, ed_date_range;
		Scanner sc = new Scanner(System.in);
		SimpleDateFormat sdf_date_range = new SimpleDateFormat("M/d/yyyy");
		
		do{
			System.out.println("\n----List Appointments of Doctor----");
			System.out.print("Doctor ID: ");
			try{
				doctor_ID = Integer.parseInt(in.readLine());
				break;
			}catch(Exception e){
				System.out.println("Doctor ID must be an integer!\n");
				continue;
			}
		}while(true);
		
		do{
			try{
				System.out.println("----Date Range----");
				System.out.print("From: ");
				st_date_range = sc.nextLine();
				sdf_date_range.parse(st_date_range);
				System.out.print("To  : ");
			  	ed_date_range = sc.nextLine();
				sdf_date_range.parse(ed_date_range);		
				break;
			}catch(ParseException e){
				System.out.println("Invalid date range! Try the format(mm/dd/yyyy)");
				continue;
			}
		}while(true);		
		
		try{
			String query  = "SELECT D.doctor_ID, D.name, A.adate, A.status\n";
			       query += "FROM Doctor D, Appointment A, has_appointment H\n";
			       query += "WHERE D.doctor_ID = H.doctor_id AND H.appt_id = A.appnt_ID AND (A.status = 'AC' OR A.status = 'AV') AND D.doctor_ID = "+ doctor_ID +" AND A.adate BETWEEN \'" + st_date_range + "\' AND \'" + ed_date_range + "\';";
			List<List<String>> result = esql.executeQueryAndReturnResult(query);
			for(int i = 0 ; i < result.size() ; i++){
				System.out.println("\nDoctor ID   : "+result.get(i).get(0));
				System.out.println("Doctor Name : "+result.get(i).get(1));
				System.out.println("Time Slot   : "+result.get(i).get(2));
				System.out.println("Status      : "+result.get(i).get(3));
			}
			System.out.println();
		}catch (Exception e){
			System.out.println(e.getMessage());
		}

	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the Department
		Scanner sc = new Scanner (System.in);
                String departmentName;
                String date;
                SimpleDateFormat sdf_date = new SimpleDateFormat("M/d/yyyy");
                List<String> dp_name = new ArrayList<String>();
			

		String q = "SELECT DISTINCT name\n FROM Department;";
		try{
			System.out.println("\n----List of All Department Names----");
			List<List<String>> dn_result = esql.executeQueryAndReturnResult(q);
			for(int i = 0 ; i < dn_result.size(); i++){
			System.out.println(dn_result.get(i).get(0));
			dp_name.add(dn_result.get(i).get(0));	
		}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}

		System.out.println("\n----List Available Appointments of Department----");
	       
        	do{
			System.out.print("Department Name: ");                              
                	departmentName = sc.nextLine();
			if(dp_name.contains(departmentName)){
				break;
			}else{
				System.out.println("Could not find the following department " + departmentName + ". Please Try Again!");
			}		
		}while(true);

               do{
                try{
                      System.out.print("Enter Date: ");
                      date = sc.nextLine();
                      sdf_date.parse(date);
                      sdf_date.setLenient(false);
                      break;
                } catch (ParseException e){
                      System.out.println("Invalid date! Try the format(mm/dd/yyyy)");
                }
            } while (true);  

              
                try{

                    String query = "SELECT DISTINCT A.appnt_ID, A.adate, A.time_slot, A.status\n";
                    query = query + "FROM Appointment A, Department D\n";
                    query = query + "WHERE D.name = " + "\'" + departmentName + "\'" + " AND A.adate = " + "\'" + date + "\'" + " AND A.status = 'AV';";
                    List<List<String>> result = esql.executeQueryAndReturnResult(query);
                    if(result.size() == 0){
			System.out.println("Could not find any avalible appoinment for Department " + departmentName + " On " + date + "\n");
		    }
		    for(int i = 0 ; i < result.size() ; i++){
                         System.out.print("\n"+result.get(i));
                         System.out.println("\n");
                    }
                } catch (Exception e){
                      System.out.println(e.getMessage());
                }     
                 
	}
	
	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
		String query =  "SELECT D.doctor_ID, D.name, "+
			        "SUM(case when A.status = 'PA' then 1 else 0 end) AS \"PA\","+
		                "SUM(case when A.status = 'AC' then 1 else 0 end) AS \"AC\","+
			        "SUM(case when A.status = 'AV' then 1 else 0 end) AS \"AV\","+
			        "SUM(case when A.status = 'WL' then 1 else 0 end) AS \"WL\""+
			        "FROM Doctor D, Appointment A, has_appointment H\n"+
				"WHERE D.doctor_ID = H.doctor_id\n"+
				"AND H.appt_id = A.appnt_ID\n"+
				"GROUP BY D.doctor_ID, A.status\n"+
				"ORDER BY D.doctor_ID DESC;";
		try{
		    List<List<String>> result = esql.executeQueryAndReturnResult(query);
		    for(int i = 0 ; i < result.size() ; i++){
			System.out.println("\nDoctor ID           : " + result.get(i).get(0));
			System.out.println("Doctor              : " + result.get(i).get(1));
			System.out.println("Type of Appointment : PC: " + result.get(i).get(2) + " AC: " + result.get(i).get(3) + " AV : " + result.get(i).get(4) + " WL : " + result.get(i).get(5));
                    }
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
		System.out.println();
	}

	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
	}
}
