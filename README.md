# EPOS Resources-Service

The **Resources-Service** is a core component of the EPOS Integrated Core Services Central (ICS-C) infrastructure. It provides RESTful APIs to manage, query, and expose metadata records describing digital assets—such as datasets, software, and services—compliant with the EPOS-DCAT-AP model. This service acts as the primary interface for accessing the EPOS Metadata Catalogue, enabling interoperability across the EPOS ecosystem.

---

## Table of Contents

* [Overview](#overview)
* [Key Features](#key-features)
* [Architecture](#architecture)
* [Installation](#installation)
* [Configuration](#configuration)
* [API Usage](#api-usage)
* [Development](#development)
* [Contributing](#contributing)
* [License](#license)

---

## Overview

The Resources-Service is designed to:

* Serve as the authoritative access point for metadata records ingested into the EPOS Metadata Catalogue.
* Provide RESTful APIs for querying and retrieving metadata records.
* Ensure compliance with the EPOS-DCAT-AP model, facilitating interoperability and adherence to FAIR principles.
* Integrate seamlessly with other EPOS services, such as the Ingestor-Service and the EPOS Data Portal.

---

## Key Features

* **Metadata Retrieval**: Access metadata records describing digital assets provided by Thematic Core Services (TCSs).
* **RESTful API**: Expose endpoints for querying metadata based on various criteria.
* **Compliance**: Ensure metadata conforms to the EPOS-DCAT-AP model, an extension of DCAT-AP tailored for the EPOS community.
* **Integration**: Work in conjunction with other EPOS services to provide a cohesive metadata management ecosystem.([Epos EU][1])

---

## Architecture

The Resources-Service is built using a microservices architecture, leveraging Spring Boot for rapid development and deployment. It interacts with a relational database to store and retrieve metadata records. The service is designed to be stateless, ensuring scalability and ease of maintenance.([Epos EU][2])

**Components:**

* **API Layer**: Handles incoming HTTP requests and routes them to the appropriate service components.
* **Service Layer**: Contains the business logic for processing metadata queries and interactions.
* **Persistence Layer**: Manages database interactions, ensuring efficient storage and retrieval of metadata records.

---

## Installation

**Prerequisites:**

* Java 11 or higher
* Maven 3.6 or higher
* Relational Database (e.g., PostgreSQL)

**Steps:**

1. **Clone the Repository:**

   ```bash
   git clone https://github.com/epos-eu/resources-service.git
   cd resources-service
   ```



2. **Build the Application:**

   ```bash
   mvn clean install
   ```



3. **Configure Environment Variables:**

   Set the necessary environment variables, such as `BASECONTEXT`, to configure the application's context path.

4. **Run the Application:**

   ```bash
   java -jar target/resources-service.jar
   ```



---

## Configuration

The application can be configured using environment variables or application properties. Key configurations include:([Epos EU][1])

* **Database Settings**: Configure the database URL, username, and password.
* **Server Port**: Set the port on which the application will run.
* **Logging**: Adjust logging levels and formats as needed.([PMC][3])

*Note*: Ensure that the database schema is initialized before starting the application.

---

## API Usage

The Resources-Service exposes RESTful endpoints for interacting with metadata records. While specific endpoint details are not provided in the repository, typical operations include:

* **Retrieve Metadata Record**: Fetch a metadata record by its unique identifier.
* **Search Metadata**: Query metadata records based on criteria such as keywords, resource type, or provider.
* **List All Records**: Retrieve a list of all available metadata records.

*Note*: For detailed API specifications, refer to the Swagger documentation if available or inspect the controller classes within the source code.

---

## Development

**Project Structure:**

* **`src/main/java`**: Contains the main application code, including controllers, services, and models.
* **`src/test/java`**: Includes unit and integration tests.
* **`resources`**: Holds application properties and configuration files.([ResearchGate][4])

**Building the Project:**

Use Maven to build the project:

```bash
mvn clean install
```



**Running Tests:**

Execute the test suite using Maven:([ResearchGate][5])

```bash
mvn test
```


---

## Contributing

Contributions are welcome! To contribute:

1. **Fork the Repository**: Create your own fork of the project.
2. **Create a Branch**: Develop your feature or fix in a new branch.
3. **Commit Changes**: Ensure your commits are well-documented.
4. **Push to Fork**: Push your changes to your forked repository.
5. **Submit a Pull Request**: Open a pull request detailing your changes.([ResearchGate][6])

*Note*: Please adhere to the project's coding standards and include relevant tests for your contributions.

---

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.

---

For more information on the EPOS infrastructure and related services, visit the [EPOS Open Source](https://epos-eu.github.io/epos-open-source/) page.

---

[1]: https://epos-eu.github.io/EPOS-DCAT-AP/v3/ "EPOS-DCAT-AP - Version 3.0"
[2]: https://epos-eu.github.io/epos-open-source/ "EPOS Platform Open Source"
[3]: https://pmc.ncbi.nlm.nih.gov/articles/PMC10632364/ "The EPOS multi-disciplinary Data Portal for integrated access to ..."
[4]: https://www.researchgate.net/figure/IS-EPOS-platform-services-implementation-based-on-the-InSilicoLab-framework_fig3_312523822 "-IS-EPOS platform services implementation based on the InSilicoLab ..."
[5]: https://www.researchgate.net/figure/EPOS-Functional-Architecture_fig1_266268820 "EPOS Functional Architecture | Download Scientific Diagram"
[6]: https://www.researchgate.net/figure/Main-elements-of-the-EPOS-Architecture-NRIs-TCS-and-ICS-ICS-C-ICS-D-form-the-EPOS_fig1_360272120 "Main elements of the EPOS Architecture: NRIs, TCS and ICS (ICS-C ..."
