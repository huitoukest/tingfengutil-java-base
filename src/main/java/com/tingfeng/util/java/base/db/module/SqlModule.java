package com.tingfeng.util.java.base.db.module;

/**
 * SQL module interface, each SQL clause as a module.
 * Defines the contract for SQL building blocks like SELECT, FROM, WHERE, etc.
 */
public interface SqlModule {

    /**
     * Get the SQL clause type identifier.
     *
     * @return the module type name
     */
    String getType();

    /**
     * Build and return the SQL fragment for this module.
     *
     * @return the SQL fragment string
     */
    String build();

    /**
     * Check if this module has content to build.
     *
     * @return true if module has valid content
     */
    boolean isValid();

    /**
     * Get the parameters used in this module for prepared statement.
     *
     * @return array of parameter values
     */
    Object[] getParams();
}
