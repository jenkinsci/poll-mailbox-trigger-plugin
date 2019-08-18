Feature: Configuration

  Background: The tests are setup
    Given the plugin is initialised


  Scenario: I want the default config values to be IMAPS
    When I set the configuration to
      | Host | Username | Password | Script |
    Then the effective configuration should be
      | folder              | INBOX     |
      | mail.debug          | false     |
      | mail.debug.auth     | false     |
      | mail.imaps.port     | 993       |
      | receivedXMinutesAgo | 1440      |
      | storeName           | imaps     |
      | subjectContains     | jenkins > |


  Scenario: I want to be able to set the basic config values
    When I set the configuration to
      | Host     | Username | Password | Attachments|
      | mail.com | morty    | chickens | IGNORE     |
    Then the effective configuration should be
      | attachments         | IGNORE    |
      | folder              | INBOX     |
      | host                | mail.com  |
      | mail.debug          | false     |
      | mail.debug.auth     | false     |
      | mail.imaps.host     | mail.com  |
      | mail.imaps.port     | 993       |
      | password            | snekcihc  |
      | receivedXMinutesAgo | 1440      |
      | storeName           | imaps     |
      | subjectContains     | jenkins > |
      | username            | morty     |


  Scenario: I want to be able to override the default config values
    When I set the configuration to
      | Host | Username | Password |Attachments|
      | aaa  | bbb      | ccc      |IGNORE     |
    And the script
    """
    mail.debug=true
    mail.debug.auth=true
    mail.fff.host=xxx
    mail.fff.port=111
    receivedXMinutesAgo=222
    subjectContains=ddd
    storeName=fff
    folder=eee
    mail.fff.timeout=333
    mail.fff.connectiontimeout=444
    """
    Then the effective configuration should be
      | attachments                | IGNORE |
      | folder                     | eee    |
      | host                       | aaa    |
      | mail.debug                 | true   |
      | mail.debug.auth            | true   |
      | mail.fff.connectiontimeout | 444    |
      | mail.fff.host              | xxx    |
      | mail.fff.port              | 111    |
      | mail.fff.timeout           | 333    |
      | password                   | ccc    |
      | receivedXMinutesAgo        | 222    |
      | storeName                  | fff    |
      | subjectContains            | ddd    |
      | username                   | bbb    |


  Scenario: I want to be able to use Jenkins environment variables in my configuration
    Given the following Jenkins variables
      | cat     | meow  |
      | dog     | woof  |
      | pig     | oink  |
      | cow     | wazoo |
      | truthy  | true  |
      | falsey  | false |
      | numeric | 80085 |
    When I set the configuration to
      | Host       | Username   | Password   |Attachments|
      | aa $cat aa | bb $dog bb | cc $pig cc |IGNORE     |
    And the script
    """
    mail.debug=$truthy
    mail.debug.auth=${falsey}
    receivedXMinutesAgo=$numeric
    subjectContains=dd ${cow} dd
    folder=ee ${dog} ee
    mail.imaps.connectiontimeout=${numeric}
    """
    Then the effective configuration should be
      | attachments                  | IGNORE      |
      | folder                       | ee woof ee  |
      | host                         | aa meow aa  |
      | mail.debug                   | true        |
      | mail.debug.auth              | false       |
      | mail.imaps.connectiontimeout | 80085       |
      | mail.imaps.host              | aa meow aa  |
      | mail.imaps.port              | 993         |
      | password                     | cc knio cc  |
      | receivedXMinutesAgo          | 80085       |
      | storeName                    | imaps       |
      | subjectContains              | dd wazoo dd |
      | username                     | bb woof bb  |


  Scenario: I want to be able to clear the default config values
    When I set the configuration to
      | Username | Password |Attachments|
      | bbb      | ccc      |IGNORE      |
    And the script
    """
    folder=
    mail.debug=
    mail.debug.auth=
    mail.imaps.port=
    receivedXMinutesAgo=
    storeName=
    subjectContains=
    """
    Then the effective configuration should be
      | attachments | IGNORE |
      | password    | ccc    |
      | username    | bbb    |


  Scenario: I want default values for IMAP
    When I set the configuration to
      | Host     | Username | Password |Attachments|
      | mail.com | morty    | chickens |IGNORE      |
    And the script
    """
    storeName=imap
    """
    Then the effective configuration should be
      | attachments         | IGNORE    |
      | folder              | INBOX     |
      | host                | mail.com  |
      | mail.debug          | false     |
      | mail.debug.auth     | false     |
      | mail.imap.host      | mail.com  |
      | mail.imap.port      | 143       |
      | password            | snekcihc  |
      | receivedXMinutesAgo | 1440      |
      | storeName           | imap      |
      | subjectContains     | jenkins > |
      | username            | morty     |


  Scenario: I want default values for IMAPS
    When I set the configuration to
      | Host     | Username | Password |Attachments|
      | mail.com | morty    | chickens |IGNORE    |
    And the script
    """
    storeName=imaps
    """
    Then the effective configuration should be
      | attachments         | IGNORE    |
      | folder              | INBOX     |
      | host                | mail.com  |
      | mail.debug          | false     |
      | mail.debug.auth     | false     |
      | mail.imaps.host     | mail.com  |
      | mail.imaps.port     | 993       |
      | password            | snekcihc  |
      | receivedXMinutesAgo | 1440      |
      | storeName           | imaps     |
      | subjectContains     | jenkins > |
      | username            | morty     |


  Scenario: I want default values for POP3
    When I set the configuration to
      | Host          | Username        | Password |Attachments|
      | pop.gmail.com | morty@gmail.com | chickens |IGNORE    |
    And the script
    """
    storeName=pop3
    """
    Then the effective configuration should be
      | attachments         | IGNORE          |
      | folder              | INBOX           |
      | host                | pop.gmail.com   |
      | mail.debug          | false           |
      | mail.debug.auth     | false           |
      | mail.pop3.host      | pop.gmail.com   |
      | mail.pop3.port      | 110             |
      | password            | snekcihc        |
      | storeName           | pop3            |
      | subjectContains     | jenkins >       |
      | username            | morty@gmail.com |


  Scenario: I want default values for POP3S
    When I set the configuration to
      | Host          | Username        | Password |Attachments|
      | pop.gmail.com | morty@gmail.com | chickens |IGNORE    |
    And the script
    """
    storeName=pop3s
    """
    Then the effective configuration should be
      | attachments         | IGNORE          |
      | folder              | INBOX           |
      | host                | pop.gmail.com   |
      | mail.debug          | false           |
      | mail.debug.auth     | false           |
      | mail.pop3s.host     | pop.gmail.com   |
      | mail.pop3s.port     | 995             |
      | password            | snekcihc        |
      | storeName           | pop3s           |
      | subjectContains     | jenkins >       |
      | username            | morty@gmail.com |