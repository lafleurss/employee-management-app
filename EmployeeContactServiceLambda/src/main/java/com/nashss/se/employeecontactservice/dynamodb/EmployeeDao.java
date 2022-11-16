package com.nashss.se.employeecontactservice.dynamodb;
import com.nashss.se.employeecontactservice.dynamodb.models.Employee;
import com.nashss.se.employeecontactservice.exceptions.EmployeeNotFoundException;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Accesses data for a playlist using {@link Employee} to represent the model in DynamoDB.
 */
public class EmployeeDao {

    private static final int PAGE_SIZE = 20;
    private final DynamoDBMapper dynamoDBMapper;

    /**
     * Instantiates a EmployeeDao object.
     *
     * @param dynamoDBMapper the {@link DynamoDBMapper} used to interact with the employees table
     */
    @Inject
    public EmployeeDao(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    /**
     * Returns the {@link Employee} corresponding to the specified id.
     *
     * @param employeeId the Employee ID
     * @return the stored Employee, or null if none was found.
     */

    public Employee getEmployee(String employeeId) {
        Employee employee = dynamoDBMapper.load(Employee.class, employeeId);
        if (null == employee) {
            throw new EmployeeNotFoundException(String.format("Could not find Employee with ID '%s' ", employeeId)
            );

        }
        return employee;
    }

    /**
     * Returns the list of {@link Employee} starting at the supplied ID.
     *
     * @param employeeStartKey the Employee ID
     * @param forward boolean if true the page will go to the next if false it will go to previous.
     * @return the stored Employees, or null if none was found.
     */

    public List<Employee> getAllActiveEmployeesWithLimit(String employeeStartKey, Boolean forward) {
        Map<String, AttributeValue> startKeyMap = new HashMap<>();
        startKeyMap.put("employeeStatus", new AttributeValue().withS("Active"));
        startKeyMap.put("employeeId", new AttributeValue().withS(employeeStartKey));

        Map<String, AttributeValue> valueMap = new HashMap<>();
        valueMap.put(":employeeStatus", new AttributeValue().withS("Active"));
        DynamoDBQueryExpression<Employee> queryExpression = new DynamoDBQueryExpression<Employee>()
                .withIndexName(Employee.EMPLOYEE_STATUS)
                .withLimit(PAGE_SIZE)
                .withScanIndexForward(forward)
                .withConsistentRead(false)
                .withExclusiveStartKey(startKeyMap)
                .withKeyConditionExpression("employeeStatus = :employeeStatus")
                .withExpressionAttributeValues(valueMap);

        return dynamoDBMapper.queryPage(Employee.class, queryExpression).getResults();

    }

    /**
     * Creates the provided employee in the Employees DynamoDB table.
     *
     * @param employeeToCreate the employee to be created in the table
     */
    public void createEmployee(Employee employeeToCreate) {
        dynamoDBMapper.save(employeeToCreate);
    }
}
