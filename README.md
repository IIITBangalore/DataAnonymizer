# Data Anonymizer

# Open Source Data Anonymization Software

## Introduction:

In recent years, escalation of technology led to an increase in the capability to record and
store personal data about consumers and individuals. With this information almost anyone
can track or know more about a person’s life. This raised concerns on personal data misuses
in many different ways. To mitigate these issues, some de-identification methodologies have
recently been proposed that, in some well controlled circumstances, allow for the re-use of
personal data in privacy- preserving ways. So one such particular method is Data
Anonymization.

Data anonymization ensures that even if de-identified data is stolen, it is very hard to re-
identify it.

## Objectives:

The main objective of this web application is to make it easy for a user to anonymize the
data. It means the user can upload the dataset and can choose configuration or create a new
configuration with in the web interface for de-identification, after that the de-identified or
anonymised data can be downloaded. The important task of this project was to create an
interface for the user such that a new configuration can be developed on the dataset. The tool
internally uses **_ARX Library_** for de-identification.

## Development Setup

The Development of this tool can be done using Eclipse as an IDE. Clone this repository and
import it to Eclipse and make sure all the dependencies are present.
More information about it can be found in the complete documentation of project. Support
for further IDEs such as IntelliJ IDEA and Maven is experimental.


### How to install:

There are different ways to run data anonymizer-

Using War file -

```
Download the DataAnonymizer.war file from the target folder of this repository and
copy it to the webapps directory of tomcat and start the tomcat server.
```
```
To start the server, go to tomcat directory in the terminal and use the following
command - ./bin/startup.sh
For Ubuntu : /var/lib/tomcat7/webapps
For tomcat install, you can refer Installing Apache Tomcat on Linux
For Deployment of web application on tomcat refer here
```
Using docker image -

```
Download the Docker image for Data Anonymizer from here or run the below
command to download the file.
```
```
Downloading docker image: $ docker pull pranith563/data_anonymizer:webimg
To run docker container: $ docker run -i -t pranith563/data_anonymizer:webimg
```
Using Eclipse IDE -
Open eclipse and choose import and copy the git url to import the project and
run it using apache.

You can access the data anonymizer at -
[http://localhost:8080/DataAnonymizer/page-login.html](http://localhost:8080/DataAnonymizer/page-login.html)

Default details to access data anonymizer are:
```
username: admin@iiitb
Password: admin
```
## License :

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
except in compliance with the License. You may obtain a copy of the License at


[http://www.apache.org/licenses/LICENSE-2.](http://www.apache.org/licenses/LICENSE-2.)

Unless required by applicable law or agreed to in writing, software distributed under the
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
OF ANY KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.

#### Citations and References:

**ARX Library- [http://arx.deidentifier.org/](http://arx.deidentifier.org/)**
Prasser F., Kohlmayer, F. (2015). Putting statistical disclosure control into practice: The ARX
data anonymization tool. In Medical Data Privacy Handbook (pp. 111-148). Springer, Cham.