# F-Secure Cyber Security Base - Course Project I

This is a course project #1 for [Cyber Security Base with F-Secure](https://cybersecuritybase.github.io/)
and University of Helsinki in collaboration with F-Secure Cyber Security Academy that focuses on building
core knowledge and abilities related to the work of a cyber security professional.

My task was to create a web application that has at least five different flaws from the OWASP top ten
list (https://www.owasp.org/index.php/Top_10_2013-Top_10).

I'm no expert in Java Spring framework, but decided to do this course project with it since there are
always new and great things to learn. During the project I briefly learned the framework too.

For this project I extended the provided [template](https://github.com/cybersecuritybase/cybersecuritybase-project)
in a way that it provides additional features such as:

- More input fields for sign up process
- User sign up modification with password based authentication
- Admin module to list, edit and delete sign up

To get up and running with this project you need to clone it into your computer and have Net Beans IDE
installed. After cloning you will get pre configured and bundled Net Beans project ready to open.

Open the project with Net Beans IDE by clicking File -> Open project. Then click "Run", point your browser
into http://localhost:8080 and start experiencing.

## Found security issues

### Sensitive Data Exposure

It is possible to iterate every sign up by fuzzing attribute called "id" in the url when editing a sign up.

Steps to reproduce:

1. Make two different sign ups. To make a sign up point your browser to http://localhost:8080/form.
    1. First sign up:
        1. First name: John
        2. Last name: Doe
        3. Email: john.doe@local
        4. Phone number: 1234567890
        5. Address: 123 Main street
        6. Password: 123456
    2. Second sign up:
        1. First name: Charlie
        2. Last name: Rhodes
        3. Email: charlie.rhodes@local
        4. Phone number: 0987654321
        5. Address: 56 Manhattan avenue
        6. Password: 654321
2. Go and edit your first sign up. To do so click "Edit"
3. Input your email and password. john.doe@local, 1234546
4. In the url field you see an "id" attribute stating "1". http://localhost:8080/edit?id=1
5. Change this id number to "2".
6. As a result you will be able to edit Charlie's attendee details.
7. Test that this really worked by logging in as a Charlie. charlie.rhodes@local, 654321 and go edit her details.

#### Fix

To get this issue fixed; system should not keep id number of a user in a place that can be
easily tampered. For instance storing the user id value in the session makes it possible for attacker not to tamper with
data client side. Sessions are stored at server side so tampering this data needs access to server. In addition system
should check that currently "logged in" user has a permission to view and edit desired user details.

### Broken Authentication and Session Management

It is possible to brute force the admin panel login with tools such as Owasp ZAP.

1. Open Owasp ZAP and locate “Quick start” tab
1. As a URL to Attack input http://localhost:8080
1. Click Attack and let the scan complete
1. Locate “Spider tab” and sub button “Show messages”. Then search for a line containing http://localhost:8080/admin/login
1. Click the line and click “Request” tab above
1. You should see two boxes where the bottom one says “password=ZAP”
1. Double click the word ZAP and then right click on it
1. From menu, select Fuzz
1. Make sure that “ZAP” word is selected and then click “Add”
1. On "Add payload" window select "File" as a type and put 10_million_password_list_top_1000.txt file as a source. Found here: https://github.com/danielmiessler/SecLists/blob/master/Passwords/10_million_password_list_top_1000.txt
1. Click "Add"
1. Click "Ok"
1. Click "Start Fuzzer"
1. From the Fuzzer results we can conclude that admin password is weak. Defined as a "victory".

#### Fix

System should prevent login attempts for a n-amount of time if certain quantity of the login attempts has been
performed. Login attempts can be limited as per ip address or other unique client side identifier. Also implementing a CSRF
protection for the login form makes it harder an attacker to automate such a process. In addition one may
implement a captcha support such as [reCAPTCHA](https://www.google.com/recaptcha/intro/index.html) to prevent bots.

### Cross-Site Scripting (XSS)

When doing a sign up for a event it is possible to inject malicious javascript code in the form fields.

1. Click Sign up
1. Fill up fields as you like but in the "First name" field put the following malicious javascript:
   1. `<script>location.href = 'https://google.com';</script>`
1. Login into admin using password “victory”
1. Note that you will be redirected into google and admin panel is totally unusable

As a side note; attacker could craft malicious page looking totally like “Event platform” admin panel,
but instead it asks for your password again. This way an attacker could steal valuable user passwords.

#### Fix

All user input should be validated and sanitized before handling the values to make sure that only desired data gets
stored into system. In this project data is persisted and read as is, which causes the platform being vulnerable
to XSS attacks. As a minimum fix `th:text` should have been used instead of `th:utext` when presenting data to user.

### Cross-Site Request Forgery (CSRF)

When investigating forms and links it seems that there are no CSRF protection at all in any manner. To be able to verify
this let’s do a very simple test case.

1. Make sure that you have few test sign ups
1. Click “Admin” and login with a password "victory"
1. Notice that there is a possibility to delete a sign up record
1. It seems to work in a way that it makes a GET request into certain endpoint called http://localhost:8080/admin/delete?id=1
1. To abuse this let’s make a simple HTML-file with a image in it:
    1. `<img src="http://localhost:8080/admin/delete?id=1" />`
1. Save the file with a name test.html
1. Open new tab to your browser
1. Open our test file within the new tab, you should see a broken image
1. Note that you were able to delete a record

Imagine that you as an event manager just got email with broken images. Next step you figure out is
that all your event sign ups have been disappeared. What a disaster!

#### Fix

To prevent CSRF; The client needs to use secure token within every form request. Token is then checked at server side and if
it does not match then the request is invalidated. Preferably user is also noticed regarding weird behaviour. Spring
framework by default implements CSRF protection, however it is disabled in this project.

### Security Misconfiguration

It seems that system does not protect session cookie properly by setting HTTP-Only flag. This makes it
possible for attacker to steal user session and fix it for himself. The system has a misconfiguration.

1. Go to front page http://localhost:8080
1. Open browsers developer tools, on chrome choose More tools -> Developer tools
1. Open application tab
1. From the left menu choose Cookies -> http://localhost:8080
1. Note that session name is FOOSESSIONID (not to collide other systems you may have tested)
1. See that HTTP-Only flag is not set
1. Using XSS technique described above insert this javascript into "First name" field
    1. `<script>alert(document.cookie);</script>`
1. Login to admin interface and see that your session id is exposed

#### Fix

All session cookies should be protected against access using client side scripting languages such as javascript. On modern
browsers this can be achieved by setting HTTP-Only flag on a session cookie. This project contains a piece of a code to
disable such a security feature.