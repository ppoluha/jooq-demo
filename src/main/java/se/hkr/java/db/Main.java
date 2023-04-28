package se.hkr.java.db;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import se.hkr.java.db.generated.tables.records.EmployeeRecord;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Optional;

import static se.hkr.java.db.generated.tables.Employee.EMPLOYEE;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "mauFJcuf5dhRMQrjj")) {
            DSLContext db = DSL.using(conn, SQLDialect.MYSQL);

            // configure jooq to output sql to console
            db.configuration().set(new ExecuteListener() {
                public void start(ExecuteContext ctx) {
                    System.out.println(ctx.query());
                }
            });

            // insert using normal repetition of columns and their values
            db.insertInto(EMPLOYEE, EMPLOYEE.NAME, EMPLOYEE.AGE)
                    .values("Lisa", 27)
                    .execute();

            // insert using shorthand notion if all columns are used
            db.insertInto(EMPLOYEE)
                    .set(new EmployeeRecord(0, "Lisa", 27))
                    .execute();

            // update using condition
            db.update(EMPLOYEE)
                    .set(EMPLOYEE.AGE, 32)
                    .where(EMPLOYEE.ID.eq(3))
                    .execute();

            // untyped search for retrieving any number of columns
            Result<Record> result1 = db.select()
                    .from(EMPLOYEE)
                    .fetch();
            for (Record r : result1) {
                int id = r.getValue(EMPLOYEE.ID);
                String name = r.getValue(EMPLOYEE.NAME);
                int age = r.getValue(EMPLOYEE.AGE);
                System.out.printf("Employee with id = %s, name = %s and age = %s%n", id, name, age);
            }

            // typed search for retrieving all columns for specified table into a record object
            List<EmployeeRecord> result2 = db.selectFrom(EMPLOYEE).fetch();
            for (EmployeeRecord r : result2) {
                int id = r.getId();
                String name = r.getName();
                int age = r.getAge();
                System.out.printf("Employee with id = %s, name = %s and age = %s%n", id, name, age);
            }

            // fetch only names for employees that are older than 40, note that all columns still are selected
            List<String> names = db.select()
                    .from(EMPLOYEE)
                    .where(EMPLOYEE.AGE.gt(40))
                    .fetch(EMPLOYEE.NAME);
            names.forEach(System.out::println);

            // fetch optionally one record
            Optional<EmployeeRecord> optionalEmployee = db
                    .selectFrom(EMPLOYEE)
                    .where(EMPLOYEE.ID.eq(1))
                    .fetchOptional();
            EmployeeRecord e = optionalEmployee
                    .orElseThrow(() -> new IllegalArgumentException("Invalid employee id!"));
            System.out.printf("Employee with id = 1: Name = %s%n", e.getName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
