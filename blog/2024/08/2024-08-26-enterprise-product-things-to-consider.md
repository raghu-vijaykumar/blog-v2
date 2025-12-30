---
slug: enterprise-product-things-to-consider
title: 'Enterprise Product - Things to consider'
tags: ["architecture", "enterprise"]
authors: [raghu-vijaykumar]
date: 2024-08-26
---

![Enterprise Product - Things to consider](./assets/cover.jpeg)

Developing an enterprise product is a complex endeavour that demands meticulous planning and execution. Here's a comprehensive checklist that can serve as your roadmap to success. Whether you're launching a web server or a multifaceted application, these steps are critical for ensuring business continuity

<!-- truncate -->

## Business Case

- Business Need is defined
- Product architecture and high level technical architecture is determined
- High-level technical plan is devised
- Estimated Budgets allocated
- Migration strategy, if applicable, is established
- Service Delivery Procedures are determined
- Necessary resources assigned
- Definite Commitment to technical and security standards established.
- Retirement and Disposal plans are identified, if needed

## Architecture

- Context Diagrams showing major parts of the system with interactions
- Sequence Diagrams to show actor, component, service, object interactions over time and the types of message they exchange
- Service Maps to understand dependencies and interfaces between systems
- Data Flow Diagram (DFD) representing the "flow" of data through system
- Deployment Topology diagrams
- Strategy - Public/Private/Hybrid Cloud? - Reasons for selection.
- Compliance - List all SaaS/on-premise Technologies/Services/API's leveraged in public/private cloud, and confirm that all are approved to use in your company

## Environment

- Product/Services deployed Platform Account Info (GCP Project ID, AWS Account Number, Azure Subscription Number, Private Cloud Platform/Data Center Location)
- Fully documented Environment details include (Memory, CPU, Disk, FQDN, IP, Port, Load Balancer URL, Health Endpoint, DB Connection Details (NO clear text Passwords), Shared Storage details, etc).

## Build, Deploy, Test & Release

- Have a Source Control, Artifact Repository, Container Registry.
- Automated Build Pipeline, Code Coverage/Unit Tests, Code Quality Analysis, Static Code analysis.
- Automated Infrastructure provisioning pipeline. Stop/Start scripts for Infra/App available
- Automated deployment pipeline, rollback of failed deployment
- Test Cases, Test Plans, Test Strategy, and Acceptance Criteria defined
- Post Deployment Certification/Smoke/Sanity Tests automated part of CI to exercising all dependencies
- Automated Functional Integration/Regression Tests available
- Release process automated, Releases versioned in source control, binary artifact repositories for audit/traceability purposes
- Release Notes generated/documented with all changes and relevant information
- Non-blocking known issues documented through release notes

## Performance, Capacity & Scaling

- Provide agreed upon Non Functional Requirements (NFRs) with SLA & SLI/SLO.
- Test - Peak Load Test (1.5 times of requested load volume part of requirements), Stress Test (2 times of requested load volume part of requirements) and Endurance Test conducted and passed per the agreed upon requirements, published the results and system behaviour under load
- Test - Test and Provide Capacity and Scaling Factors of each service
- Monitoring - Latency / Response Time, Traffic, Saturation & Errors

## Metering

- Application/service footprint cost, and Evidence of minimum footprint deployed across SDLC and scale with volume
- Resource tagging strategy is followed and provides filters for cost aggregation by product/service/environment. Show the evidence of each resource is tagged
- Software License Compliance. Provide a full list of software Bill of Material deployed in each SDLC environment and license type, license expiration date for commercial software, demonstrate the process in place for reminders of renewals.

## Monitoring, Logging & Alerting

- Infrastructure monitoring
- API health endpoint monitoring
- Transaction Performance monitoring against SLI/SLO/SLA
- Performance and Capacity monitoring
- Synthetic Availability monitoring
- SSL Certificate monitoring
- Dependency service monitoring
- Network Monitoring
- Aggregate/Centralized logging for all services implemented, provide Logging dashboard
- Logs should be clean and contain necessary events to help troubleshoot issues
- Automate alerting for human intervention required events
- Automated proactive alerts setup to notify the services level thresholds

## Reliability & Availability

### HA Strategy

- Multi-Zone & Multi-Region Active-Active
- Multi-Zone & Multi-Region Active-Standby for automatic DR
- Multi-Zone & Multi-Region Active-Passive Standby for manual DR to meet agreed upon Recovery Time Objective (RTO) & Recovery Point Objective (RPO)
- Multi-Zone Single-Region (where multi-region not available) Active-DR Standby for manual DR to meet agreed upon RTO & RPO

### HA Testing

- Test and document the evidence of auto scale of each component working as expected
- Confirm each Product/Service deployed to more than 3 Availability Zones (where available/applicable).
- Provide availability zone failure test results to demonstrate application withstand more than one zone failure
- Confirm each Product/Service deployed to 2 regions (where available/applicable). Provide region failure test results to demonstrate application withstand complete region failure

## Zero Downtime Deployment Model

- Blue Green deployment for reducing down-time during deployment and faster rollback if necessary
- Canary deployment model employed to get early feedback and incremental change into production
- Deployment/Rollback Procedures fully Automated/Documented to reduce the downtime for all components (Service, Host, Network, DB, Storage, etc.))

## Business Continuity & Disaster Recovery

- Define RTO and RPO working with Product/Business
- Recovery Time Objective - Maximum tolerable time to restore service in case of disaster without consequences
- Recovery Point Objective - Maximum tolerable amount of data to lose in case of disaster without consequences
- BC & DR Plan - Developed per the BC & DR compliance guidelines. Provide the Plan. Schedule the DR drill with the DR team. BC Plan - Is a strategy to ensure continuity of operations with minimal disruption. DR Plan - Is to restore data and critical systems in the event of disaster
- DR Testing - Disaster Recovery Test Execution with PITR (point-in-time recovery) with DR team.
- DR Documentation - Disaster Recovery exercise results documented.

## Backup & Restore

- Use the RPO to determine backup frequency/schedule
- Product to define data retention policy
- Persistent data backed-up?
- Restore procedures tested?

## Resilience

- Chaos Monkey Test Plan Defined
- Chaos Monkey Test Executed and documented the results
- Failover Strategy - Define strategy to detect and restore/failover when internal/external dependency failed/unavailable
- Failover Testing - Simulate internal/external dependency failures/unavailable scenarios and failover alternate defined in the failover strategy and document the results

## Security & Compliance

- Product has gone through the security team's clearance and advice.
- List of all open issues, deviations or exceptional cases have gone through the architectural review process done by the enterprise architectural team.
- Cloud Ops RACI (Responsible, Accountable, Consulted, Informed) checked, shows appropriate responsibilities to support cloud systems
- Define the enterprise logging & alert patterns for security compliance, confirm the logging receives the events from all hosts.
- Security/Compliance monitoring Endpoint Security Agents deployed and reporting back to the hub. Confirm all the hosts are reporting correctly.
- FIM (File Integrity Monitoring) configured and enabled for compliance
- Threat Vulnerability Management scans performed and remediated any identified vulnerabilities per security guidelines
- SAST (Static Application Security Testing) / Static Code Analysis performed and re-mediated any identified vulnerabilities
- Open Source Software vulnerability scan performed (leveraging security approved policy) and remediated
- PEN testing performed and remediated any vulnerabilities per security guidelines and documented the PEN test results
- Data has been classified per the Data Classification and Handling and Cryptographic Protections Technical Requirements a consultation was completed for any clarification or disputes
- Privacy Assessment criteria was examined and was completed if needed
- WAF enabled for external facing endpoints
- Base Server & Container Images should be approved by security
- PROD Environment should not contain any dependencies to non prod network/applications
- Regulatory Compliance - Identify regulatory compliance PCI DSS/FISMA/FedRAMP/GDPR/BREXIT/etc. Provide evidence for regulatory compliance requirements met.
- Data Residency Requirements Identify any data residency requirement per Country/Geo regulations, and provide the evidence of requirements met.

## Data Protection

- All PII Data at rest and in-transit identified and handled per Security/Arch guidelines
- Vaulting methods for Accounts & Keys
- Decryption keys protection stored where/who has access to them?
- Encryption & Tokenization mechanism

## Access Control

- Least use of Privileged access, Separation of duties implemented.
- Role based access for end users (MFA if applicable at least for privileged users)
- Service accounts leveraged to run services
- Key rotation policy implemented
- Service accounts password managed by a secrets management tool.
- Production access reviewed for compliance on a recurring basis.
- Data access protected using IAM access, and should be accessed from network

## ITSM Process & Procedures

- Configuration Management Database (CMDB) - Business Application Created & Business Services linked to it, provide link to ServiceNow Business Application
- CMDB - Add/Update CMDB assets/attributes/tags (Business Application ID, Application Services ID, Compliance, etc.) with each deployment in different SDLC environments.
- CMDB - Confirm that all the assets, applications/services, and its dependencies are updated correctly in CMDB.
- Release/Change/Incident Management process defined and followed per guidelines.
- Monitoring/Alerting process integrated with Incident management system.
- Change, Incident Management metrics dashboards are available

## Support & Dry Run

- Production Support Roles and Responsibilities (RACI) defined
- Contacts and Escalation procedures defined
- Support On-call rotation, and automated alert escalation process defined
- Communication (Internal/External) stakeholders defined
- Maintenance schedules defined and socialized with stakeholders
- Patch Management procedures established
- Operational/Application run books, User/API guides provided and reviewed the same with stakeholders
- Support training should occur for support personnel, and Professional Services
- Conduct the support dry run and validate the defined roles, run books, and escalation procedures worked as expected
