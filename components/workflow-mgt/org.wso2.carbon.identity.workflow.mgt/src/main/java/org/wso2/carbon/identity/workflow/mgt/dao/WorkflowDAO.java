/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.workflow.mgt.dao;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.Workflow;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.util.SQLConstants;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Workflow related DAO operation provides by this class
 *
 */
public class WorkflowDAO {

    private final String errorMessage = "Error when executing the SQL query ";

    /**
     * Adding a workflow
     *
     * @param workflow Workflow bean object
     * @param tenantId Tenant ID
     * @throws InternalWorkflowException
     */
    public void addWorkflow(Workflow workflow, int
            tenantId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.ADD_WORKFLOW_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflow.getWorkflowId());
            prepStmt.setString(2, workflow.getWorkflowName());
            prepStmt.setString(3, workflow.getWorkflowDescription());
            prepStmt.setString(4, workflow.getTemplateId());
            prepStmt.setString(5, workflow.getWorkflowImplId());
            prepStmt.setInt(6, tenantId);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException(errorMessage , e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Get a Workflow object for given workflowid
     *
     * @param workflowId Workflow unique id
     * @return Workflow object
     * @throws InternalWorkflowException
     */
    public Workflow getWorkflow(String workflowId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String query = SQLConstants.GET_WORKFLOW;

        Workflow workflow = null;

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String workflowName = rs.getString(SQLConstants.WF_NAME_COLUMN);
                String description = rs.getString(SQLConstants.DESCRIPTION_COLUMN);
                String templateId = rs.getString(SQLConstants.TEMPLATE_ID_COLUMN);
                String implId = rs.getString(SQLConstants.TEMPLATE_IMPL_ID_COLUMN);
                workflow = new Workflow.WorkflowBuilder()
                        .setWorkflowId(workflowId)
                        .setWorkflowName(workflowName)
                        .setWorkflowDescription(description)
                        .setTemplateId(templateId)
                        .setWorkflowImplId(implId)
                        .build();

                break;
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return workflow;
    }

    /**
     * Remove Workflow from the DB
     *
     * @param workflowId workflow Id
     * @throws InternalWorkflowException
     */
    public void removeWorkflow(String workflowId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.DELETE_WORKFLOW_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowId);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Remove all workflows of a given tenant id.
     *
     * @param tenantId Id of the tenant
     * @throws InternalWorkflowException
     */
    public void removeWorkflows(int tenantId) throws InternalWorkflowException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                    .DELETE_WORKFLOW_BY_TENANT_ID_QUERY)) {
                prepStmt.setInt(1, tenantId);
                prepStmt.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        }
    }

    /**
     * Update current workflow
     *
     * @param workflow Workflow object
     * @throws InternalWorkflowException
     */
    public void updateWorkflow(Workflow workflow)
            throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.UPDATE_WORKFLOW_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflow.getWorkflowName());
            prepStmt.setString(2, workflow.getWorkflowDescription());
            prepStmt.setString(3, workflow.getTemplateId());
            prepStmt.setString(4, workflow.getWorkflowImplId());
            prepStmt.setString(5, workflow.getWorkflowId());
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Retrieve Workflows for a tenant with pagination
     *
     * @param tenantId
     * @param filter
     * @param offset
     * @param limit
     * @throws InternalWorkflowException
     */
    public List<Workflow> listPaginatedWorkflows(int tenantId, String filter, int offset, int limit) throws InternalWorkflowException{

        String sqlQuery;
        List<Workflow> workflowList = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);) {
            String filterResolvedForSQL = resolveSQLFilter(filter);
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            sqlQuery = getSqlQuery(databaseProductName);
            try (PreparedStatement prepStmt = generatePrepStmt(databaseProductName, connection, sqlQuery, tenantId, filterResolvedForSQL, offset, limit);) {
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    while (resultSet.next()) {
                        String id = resultSet.getString(SQLConstants.ID_COLUMN);
                        String name = resultSet.getString(SQLConstants.WF_NAME_COLUMN);
                        String description = resultSet.getString(SQLConstants.DESCRIPTION_COLUMN);
                        String templateId = resultSet.getString(SQLConstants.TEMPLATE_ID_COLUMN);
                        String templateImplId = resultSet.getString(SQLConstants.TEMPLATE_IMPL_ID_COLUMN);
                        Workflow workflowDTO = new Workflow.WorkflowBuilder()
                                .setWorkflowId(id)
                                .setWorkflowName(name)
                                .setWorkflowDescription(description)
                                .setTemplateId(templateId)
                                .setWorkflowImplId(templateImplId)
                                .build();
                        workflowList.add(workflowDTO);
                    }
                }
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        }
        return workflowList;
    }

    private String getSqlQuery(String databaseProductName) throws InternalWorkflowException {

        String sqlQuery;
        if (databaseProductName.contains(WFConstant.DBProductNames.MYSQL)
                || databaseProductName.contains(WFConstant.DBProductNames.MARIADB)
                || databaseProductName.contains(WFConstant.DBProductNames.H2)) {
            sqlQuery = SQLConstants.GET_WORKFLOWS_BY_TENANT_AND_WF_NAME_MYSQL;
        } else if (databaseProductName.contains(WFConstant.DBProductNames.ORACLE)) {
            sqlQuery = SQLConstants.GET_WORKFLOWS_BY_TENANT_AND_WF_NAME_ORACLE;
        } else if (databaseProductName.contains(WFConstant.DBProductNames.MICROSOFT)) {
            sqlQuery = SQLConstants.GET_WORKFLOWS_BY_TENANT_AND_WF_NAME_MSSQL;
        } else if (databaseProductName.contains(WFConstant.DBProductNames.POSTGRESQL)) {
            sqlQuery = SQLConstants.GET_WORKFLOWS_BY_TENANT_AND_WF_NAME_POSTGRESQL;
        } else if (databaseProductName.contains(WFConstant.DBProductNames.DB2)) {
            sqlQuery = SQLConstants.GET_WORKFLOWS_BY_TENANT_AND_WF_NAME_DB2SQL;
        } else if (databaseProductName.contains(WFConstant.DBProductNames.INFORMIX)) {
            sqlQuery = SQLConstants.GET_WORKFLOWS_BY_TENANT_AND_WF_NAME_INFORMIX;
        } else {
            throw new InternalWorkflowException(WFConstant.Exceptions.ERROR_WHILE_LOADING_WORKFLOWS);
        }
        return sqlQuery;
    }

    /**
     * Create PreparedStatement
     *
     * @param DBProductName db product name
     * @param connection db connection
     * @param sqlQuery SQL query
     * @param tenantId Tenant ID
     * @param filterResolvedForSQL resolved filter for sql
     * @param offset offset
     * @param limit limit
     * @return PreparedStatement
     * @throws SQLException
     */
    private PreparedStatement generatePrepStmt(String DBProductName, Connection connection, String sqlQuery, int tenantId, String filterResolvedForSQL, int offset, int limit) throws SQLException {

        PreparedStatement prepStmt = null;
        if (DBProductName.equals(WFConstant.DBProductNames.POSTGRESQL)){
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, filterResolvedForSQL);
            prepStmt.setInt(3, limit);
            prepStmt.setInt(4, offset);
        } else {
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, filterResolvedForSQL);
            prepStmt.setInt(3, offset);
            prepStmt.setInt(4, limit);
        }
        return prepStmt;
    }

    /**
     * Retrieve all the Workflows for a tenant
     *
     * @Deprecated Use {@link #listPaginatedWorkflows(int, String, int, int)}
     * @param tenantId Tenant ID
     * @param filter Filter
     * @return List<Workflow>
     * @throws InternalWorkflowException
     */
    @Deprecated
    public List<Workflow> listWorkflows(int tenantId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<Workflow> workflowList = new ArrayList<>();
        String query = SQLConstants.LIST_WORKFLOWS_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, tenantId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString(SQLConstants.ID_COLUMN);
                String name = rs.getString(SQLConstants.WF_NAME_COLUMN);
                String description = rs.getString(SQLConstants.DESCRIPTION_COLUMN);
                String templateId = rs.getString(SQLConstants.TEMPLATE_ID_COLUMN);
                String templateImplId = rs.getString(SQLConstants.TEMPLATE_IMPL_ID_COLUMN);
                Workflow workflowDTO = new Workflow();
                workflowDTO.setWorkflowId(id);
                workflowDTO.setWorkflowName(name);
                workflowDTO.setWorkflowDescription(description);
                workflowDTO.setTemplateId(templateId);
                workflowDTO.setWorkflowImplId(templateImplId);
                workflowList.add(workflowDTO);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        }
        return workflowList;
    }

    /**
     * Resolve SQL Filter
     *
     * @param filter
     * @return
     * @throws InternalWorkflowException
     */
    private String resolveSQLFilter(String filter) {

        //To avoid any issues when the filter string is blank or null, assigning "%" to SQLFilter.
        String sqlFilter = "%";
        if (StringUtils.isNotBlank(filter)) {
            sqlFilter = filter.trim()
                    .replace("*", "%")
                    .replace("?", "_");
        }
        return sqlFilter;
    }

    /**
     * Get count of workflows with a filter
     *
     * @param tenantId
     * @param filter
     * @return
     * @throws InternalWorkflowException
     */
    public int getWorkflowsCount(int tenantId, String filter) throws InternalWorkflowException{

        int count=0;
        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        try {
            String filterResolvedForSQL = resolveSQLFilter(filter);
            prepStmt = connection
                    .prepareStatement(SQLConstants.GET_WORKFLOWS_COUNT_QUERY);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, filterResolvedForSQL);
            resultSet = prepStmt.executeQuery();
            if(resultSet.next()){
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(
                    "Error while getting the count of Association for the tenantID: " + tenantId, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return count;
    }


    /**
     * Clear all the parameters that stored under workflow Id
     *
     * @param workflowId WorkflowId
     * @throws InternalWorkflowException
     */
    public void removeWorkflowParams(String workflowId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.DELETE_WORKFLOW_PARAMS_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowId);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Clear all the parameters of all the workflows of a given tenant.
     *
     * @param tenantId Id of the tenant
     * @throws InternalWorkflowException
     */
    public void removeWorkflowParams(int tenantId) throws InternalWorkflowException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                    .DELETE_WORKFLOW_PARAMS_BY_TENANT_ID_QUERY)) {
                prepStmt.setInt(1, tenantId);
                prepStmt.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        }
    }
    
    /**
     * Add new parameter List to given workflow id
     *
     * @param parameterList Paramter List
     * @param workflowId Workflow Id
     * @throws InternalWorkflowException
     */
    public void addWorkflowParams(List<Parameter> parameterList, String workflowId, int tenantId) throws
            InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String query = SQLConstants.ADD_WORKFLOW_PARAMS_QUERY;
        try {
            for (Parameter parameter : parameterList) {
                prepStmt = connection.prepareStatement(query);
                prepStmt.setString(1, workflowId);
                prepStmt.setString(2, parameter.getParamName());
                prepStmt.setString(3, parameter.getParamValue());
                prepStmt.setString(4, parameter.getqName());
                prepStmt.setString(5, parameter.getHolder());
                prepStmt.setInt(6, tenantId);

                prepStmt.executeUpdate();
            }
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Retrieve List of Parameters for given workflow id
     *
     * @param workflowId
     * @return
     * @throws InternalWorkflowException
     */
    public List<Parameter> getWorkflowParams(String workflowId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<Parameter> parameterList = new ArrayList<>();
        String query = SQLConstants.GET_WORKFLOW_PARAMS;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String paramName = rs.getString(SQLConstants.PARAM_NAME_COLUMN);
                String paramValue = rs.getString(SQLConstants.PARAM_VALUE_COLUMN);
                String paramQName = rs.getString(SQLConstants.PARAM_QNAME_COLUMN);
                String paramHolder = rs.getString(SQLConstants.PARAM_HOLDER_COLUMN);
                if (StringUtils.isNotBlank(paramName)) {
                    Parameter parameter = new Parameter(workflowId, paramName, paramValue, paramQName, paramHolder);
                    parameterList.add(parameter);
                }
            }
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return parameterList;
    }
}
