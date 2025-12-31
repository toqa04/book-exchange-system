##### **Book Exchange & Selling Platform**

A full-featured web-based system that enables students to efficiently sell, buy, or exchange textbooks within a unified platform.

## **Technology Stack**
 
* Backend: Java Servlets
 
* Frontend: JSP, HTML, CSS, JavaScript, Bootstrap
 
* Database: MySQL
  
* Architecture: MVC (Model–View–Controller)
 
### **Design Patterns**
 
* Strategy Pattern: Implemented to provide flexible and dynamic search and filtering behavior
 
* Factory Method Pattern: Used to instantiate different listing types (Sell / Exchange)

### **Features**

* Secure user registration and authentication
  
* Creation and management of book listings (Sell / Exchange)
 
* Advanced search and filtering capabilities
 
* User-to-user messaging system
 
* Transaction workflows with proper concurrency handling
 
* Admin dashboard for managing users and platform content

### **Project Structure**

src/
main/
java/
com/bookexchange/
model/          # Domain and entity classes
dao/            # Data access layer
servlet/        # Application controllers
factory/        # Factory Method implementation
strategy/       # Search and filter strategies
util/           # Shared utility classes
webapp/
WEB-INF/
web.xml        # Application configuration
css/             # Stylesheets
js/              # JavaScript assets
images/          # Uploaded resources
jsp/             # JSP view pages
sql/
schema.sql           # MySQL database schema
sample_data.sql      # Optional seed data

## **Setup Instructions**

Create a MySQL database and execute sql/schema.sql

Configure database credentials in
src/main/java/com/bookexchange/util/DBConnection.java

Deploy the project on Apache Tomcat or any compatible servlet container

Access the application at
http://localhost:8080/bookexchange

## **Done by:**
* Khaled Ghaleb Diab Khader (161010)
* Ammar Abd-Almoneim Youssef Sakhrieh (165327)
* Toqah Ayman Ahmad Al-Zubi (160838)
