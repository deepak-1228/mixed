# user-service

## Implementation

1. User Service application has been developed using Spring Boot 2.1.3, Java 8 and Maven 3 as the build tool.

2. Application uses MySQL 5.7 as database to store user data

3. The application currently includes following operations
	a. Add New User
	b. Login User With OTP
	c. Get User Details and Profile using token 

## Setup & Running the application

Go to project Root directory and perform following operations -

1. Open the properties file `user-service/src/main/resources/application-local.properties` and change the database username and password

2. Use the SQL file `user-service/src/main/resources/mysql/install/UserSchema.sql` to create the required database and tables

3. Run command `sh bin/setup`. This will compile the code using maven build tool and run the JUnit test cases for build verification. It will generate the executable jar in `user-service/target/user.jar`.

4. To Start the application, run command `sh bin/user_service local` to start the Spring Boot Application

5. Access the application swagger interface on `http://localhost:8080/user/swagger-ui.html`

## Accessing the APIs

Call the below APIs using POSTMAN.

1. Add new user

```
POST http://localhost:8080/user/add
```

Request Body -

```
{
  "address": {
    "addressLine1": "189",
    "addressLine2": "Dwarka",
    "cityName": "New Delhi",
    "countryName": "India",
    "landmark": "South-West",
    "postalCode": "110061",
    "stateName": "New Delhi"
  },
  "anniversaryDate": null,
  "birthday": "1992-08-07",
  "email": "navuniverse@gmail.com",
  "firstName": "Naveen",
  "gender": "MALE",
  "isoCode": "IN",
  "lastName": "Kumar",
  "maritalStatus": "SINGLE",
  "middleName": null,
  "mobile": "9716554117",
  "profilePicture": null,
  "secondaryEmail": null,
  "secondaryEmailVerified": false,
  "secondaryIsoCode": null,
  "secondaryMobile": null,
  "secondaryMobileVerified": false,
  "userType": "STUDENT"
}
```


2. Login with above created user. This API will validate the mobile and user type combination and will send the OTP.

```
POST http://localhost:8080/user/auth/login
```

Request Body -

```
{
  "isoCode": "IN",
  "mobile": "9716554117"
}
```

Response Body -

```
{
    "status": true,
    "message": "OTP Sent for Login",
    "id": null,
    "errorCode": null,
    "data": null
}
```


3. Resend the OTP during the login flow. This API must be called after login request only.

```
POST http://localhost:8080/user/auth/resendOtp
```

Request Body -

```
{
  "isoCode": "IN",
  "mobile": "9716554117"
}
```

Response Body -

```
{
    "status": true,
    "message": "OTP Successfully Resent",
    "id": null,
    "errorCode": null,
    "data": null
}
```

4. Validate the OTP sent in above API.

Make sure to include the header `frontEnv` with value `local` in this request in order to use cookie authentication on local environment.

```
POST http://localhost:8080/user/auth/validateOtp
```

Request Body - 

```
{
  "isoCode": "IN",
  "mobile": "9716554117",
  "otp" : "1234"
}
```

Response Body - 

This will also set a cookie named `token` which will be used for authentication in subsequent calls.

```
{
    "status": true,
    "message": "User Login Successfull",
    "id": null,
    "errorCode": null,
    "data": {
        "id": null,
        "uuid": "70e69650-7588-48b3-bfba-cb16f89ba964",
        "createdAt": 1570818070000,
        "updatedAt": 1570818106104,
        "updatedBy": null,
        "createdBy": null,
        "status": true,
        "isoCode": "IN",
        "mobile": "9716554117",
        "mobileVerified": true,
        "email": "navuniverse@gmail.com",
        "emailVerified": false,
        "userType": "STUDENT"
    }
}
```

5. Verify the User Profile. 

Authentication will be performed using the `token` available in cookie set during validateOtp call.


```
GET http://localhost:8080/user/profile
```

Response Body -

```
{
    "status": true,
    "message": "Found User Profile for User Id",
    "id": null,
    "errorCode": null,
    "data": {
        "id": null,
        "uuid": "70e69650-7588-48b3-bfba-cb16f89ba964",
        "createdAt": 1570818070000,
        "updatedAt": 1570818106000,
        "updatedBy": null,
        "createdBy": null,
        "status": true,
        "isoCode": "IN",
        "mobile": "9716554117",
        "mobileVerified": true,
        "email": "navuniverse@gmail.com",
        "emailVerified": false,
        "userType": "STUDENT",
        "firstName": "Naveen",
        "middleName": null,
        "lastName": "Kumar",
        "secondaryEmail": null,
        "secondaryEmailVerified": false,
        "secondaryIsoCode": null,
        "secondaryMobile": null,
        "secondaryMobileVerified": false,
        "gender": "MALE",
        "profilePicture": null,
        "birthday": "1992-08-07",
        "maritalStatus": "SINGLE",
        "anniversaryDate": null,
        "address": {
            "addressLine1": "189",
            "addressLine2": "Dwarka",
            "landmark": "South-West",
            "cityName": "New Delhi",
            "stateName": "New Delhi",
            "postalCode": "110061",
            "countryName": "India"
        }
    }
}
```

6. Search Users by various parameters. All parameters are optional except URL Parameters PageNo and Limit

```
GET http://localhost:8080/user/search/{pageNo}/{limit}?RequestParameters

GET http://localhost:8080/user/search/1/10?email=navuniverse%40gmail.com&isoCode=IN&mobile=9716554117&status=true&userIds=70e69650-7588-48b3-bfba-cb16f89ba964&userIds=123&userType=STUDENT

```

Response Body -

```
{
  "status": true,
  "message": "Found 1 Users for Search Criteria",
  "id": null,
  "errorCode": null,
  "data": {
    "page": 1,
    "records": 1,
    "totalPages": 1,
    "totalRecords": 1,
    "data": [
      {
        "id": null,
        "uuid": "70e69650-7588-48b3-bfba-cb16f89ba964",
        "createdAt": 1570818070000,
        "updatedAt": 1570818106000,
        "updatedBy": null,
        "createdBy": null,
        "status": true,
        "isoCode": "IN",
        "mobile": "9716554117",
        "mobileVerified": true,
        "email": "navuniverse@gmail.com",
        "emailVerified": false,
        "userType": "STUDENT"
      }
    ]
  }
}
```

## Future Work

1. Update User Profile Options

2. Remove User Option

3. Authorization



## ACL
betaUrl - https://betaerp.stanzaliving.com/user/swagger-ui.html

produrl - userservice.stanzaliving.com/user/swagger-ui.html (https://erpdashboard.stanzaliving.com/user/swagger-ui.html)

To perform any action listed below, click on "Try It Out" to open editor, fill in required details and then click on execute to perform the action.

1. Login with your phone number to use the swagger to get authentication done

enter ISO Code as "IN" and keep userType as "STUDENT" in below API's

auth-controller --> /auth/login

auth-controller --> /auth/validateOtp

Enter otp as 1234 for beta / preprod environment and received otp for prod environment

Now you are authorized to perform any action on swagger.

2. To add a new user (this api will give error, if user is already created)

user-controller -> /add

sample payload with mandatory fields:
```
{
  "email": "narayan.murth@stanzaliving.com",
  "firstName": "Narayan",
  "gender": "MALE",
  "isoCode": "IN",
  "lastName": "Murthy",
  "mobile": "9811666520",
  "userType": "PROCUREMENT",
  "department": "BUSINESS_DEVELOPMENT"
}
```
save the uuid received in response (this will be helpful in assigning roles to this user at later stage)

3. To get uuid for an existing user by mobile number

user-controller -> /search/{pageNo}/{limit}

Enter 1 in both limit and pageNo and Mobile number to get the uuid of existing user.

4. To check what all roles / department a User is having:

acl-controller -> /acl/user/fe/{userUuid}

5. To view (or to get uuid's of) existing roles

role-controller -> /acl/roles/getRoles

6. To create new roles

role-controller -> /acl/role/add

Input the required fields to create new role. To create a parent role, use "SELF" in parentRoleUuid else enter the uuid of parent role.

save the uuid in received in response (this will be helpful in assigning roles to any user at later stage)

7. To assign a particular role to any user

acl-user-controller -> /acl/user/add/role

uuid of INDIA -> 7d0e47bf-52c0-4560-924e-e6599e56501a

to get the accessLevelEntityListUuid of cities / micromarket use https://erpdashboard.stanzaliving.com/transformationmaster/swagger-ui.html#/ 

internal-data-controller -> /internal/cities/all or /internal/micromarkets/all 

Possible values for UserType, Departments and AccessLevel :

UserType: STUDENT,PARENT,LEGAL,HR,TECH,FINANCE,PROCUREMENT,MANAGER,BD,LEADERSHIP,OPS,CONSUMER

Departments: TECH,FINANCE,HR,LEGAL,SUPERADMIN,LEADERSHIP,BUSINESS_DEVELOPMENT,OPS,PROCUREMENT,DESIGN,PROJECTS,TRANSFORMATIONS,SALES,WEB

AccessLevel: RESIDENCE,MICROMARKET,CITY,COUNTRY
