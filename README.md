🏥 Optimed - Healthcare Management System
===============================

A full-featured **Healthcare Management System** that enables **patients and doctors** to manage appointments, prescriptions, medical records, and notifications. The project follows **RBAC (Role-Based Access Control)** and consists of two services:

1.  **Main MVC Project** – Manages patients, doctors, appointments, prescriptions, and medical records.
    
2.  **Microservice for Notifications** – Handles notifications asynchronously.
    

📌 Features
-----------

### 🏥 **Patient & Doctor Management**

*   Patients and doctors can **register and log in** securely.
    
*   Doctors can **manage patient records** and **prescriptions**.
    
*   Patients can **view their medical history** and upcoming appointments.
    

### 🗓 **Appointments**

*   Patients can **schedule appointments** with doctors.
    
*   Doctors can **accept/reject** appointment requests.
    

### 💊 **Prescriptions & Medical History**

*   Doctors can **prescribe medications** and treatment plans.
    
*   Patients can **view their past prescriptions and history**.
    

### 🔔 **Notifications Microservice**

*   Sends **notifications** for **canceled and upcoming appointments**.
    

### 🏛 **RBAC (Role-Based Access Control)**

*   **Roles:** Admin, Doctor, Patient.
    
*   **Spring Security** ensures access control.
    

🛠 Tech Stack
-------------

### **Backend**

*   **Java 17**
    
*   **Spring Boot 3** (MVC, Security, JPA, REST API)
        
*   **Spring Security & Sessions** (for authentication)
    
*   **Hibernate & JPA** (for ORM)

*   **HATEOAS** (for API format)

*   **Spring Events** (for notifications)
    
*   **MySQL** (database)
    
*   **JUnit & Mockito** (testing)
    

### **Frontend**

*   **Thymeleaf** (for server-side rendering)
    
*   **Bootstrap 5** (for UI)
    
*   **JavaScript** (for dynamic elements)
    
    

🚀 How to Run the Project
-------------------------

### **1️⃣ Clone the Repository**

```ps
git clone https://github.com/KKDinev20/OptiMed
```

## **2️⃣ Run the Backend from cmd (Main Project & Microservice)**

### Main Project
```ps
cd optimed
mvn spring-boot:run
```

### Notification Microservice
```ps
cd healthcare-notifications
mvn spring-boot:run
```
## **4️⃣ Access the Application**

*    Frontend (Thymeleaf UI): http://localhost:8080/

*    Notification Microservice: http://localhost:8081/notifications