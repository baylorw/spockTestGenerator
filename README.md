# spockTestGenerator
# Purpose
A Webservice that takes in a Java source file and returns Spock tests for it. 

Obviously this doesn't write your test logic. It just generates the voluminous Spock boilerplate.
The generated file will have a test method for each method in the Java file complete with all the needed 
mocks and variables defined in `given` and `where` sections. 

# Other Purpose
Supplement to the article _Auto-Generating Spock Tests_ 
(https://computer.baylorwetzel.com/2020/08/05/auto-generating-spock-tests/).

# Usage
**Input:** A Java source file.

**Output:** A Spock test file.

**API:** `POST localhost:8080/tests/create` with a body (raw text) containing Java code.

   
