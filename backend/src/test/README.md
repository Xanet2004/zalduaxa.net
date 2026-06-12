# Backend Testing

This document explains how to run backend tests.

Run all commands from the `backend/` directory:

```bash
cd backend
```

## Run all tests

```bash
mvn test
```

## Run a specific test class

```bash
mvn test -Dtest=TestClassName
```

Example:

```bash
mvn test -Dtest=AuthServiceTest
```

## Run multiple specific test classes

```bash
mvn test -Dtest=TestClassOne,TestClassTwo
```

Example:

```bash
mvn test -Dtest=AuthServiceTest,AuthControllerTest
```

## Expected result

A successful test run should end with:

```text
BUILD SUCCESS
```

and show no failures or errors:

```text
Failures: 0, Errors: 0
```

## Notes

Warnings may appear during test execution. They do not necessarily mean the tests failed.

A test run is considered successful only if Maven finishes with `BUILD SUCCESS`.
