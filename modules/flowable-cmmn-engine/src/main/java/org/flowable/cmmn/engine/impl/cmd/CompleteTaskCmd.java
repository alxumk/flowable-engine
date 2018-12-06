/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.cmmn.engine.impl.cmd;

import static org.flowable.cmmn.engine.impl.util.CommandContextUtil.getTaskService;

import java.util.Map;

import org.flowable.cmmn.engine.impl.persistence.entity.PlanItemInstanceEntity;
import org.flowable.cmmn.engine.impl.task.TaskHelper;
import org.flowable.cmmn.engine.impl.util.CommandContextUtil;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.task.service.delegate.TaskListener;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.task.service.impl.persistence.entity.TaskLogEntryEntity;

/**
 * @author Joram Barrez
 */
public class CompleteTaskCmd implements Command<Void> {
    
    protected String taskId;
    protected Map<String, Object> variables;
    protected Map<String, Object> transientVariables;
    
    public CompleteTaskCmd(String taskId, Map<String, Object> variables, Map<String, Object> transientVariables) {
        this.taskId = taskId;
        this.variables = variables;
        this.transientVariables = transientVariables;
    }
    
    @Override
    public Void execute(CommandContext commandContext) {
        
        if (taskId == null) {
            throw new FlowableIllegalArgumentException("Null task id");
        }
        
        TaskEntity taskEntity = getTaskService(commandContext).getTask(taskId);
        if (taskEntity == null) {
            throw new FlowableObjectNotFoundException("Could not find task entity for id " + taskId, TaskEntity.class);
        }
        
        String planItemInstanceId = taskEntity.getSubScopeId();
        PlanItemInstanceEntity planItemInstanceEntity = null;
        if (planItemInstanceId != null) {
            planItemInstanceEntity = CommandContextUtil.getPlanItemInstanceEntityManager(commandContext).findById(planItemInstanceId);
            if (planItemInstanceEntity == null) {
                throw new FlowableException("Could not find plan item instance for task " + taskId);
            }
        }
        
        if (variables != null) {
            taskEntity.setVariables(variables);
        }
        if (transientVariables != null) {
            taskEntity.setTransientVariables(transientVariables);
        }

        logTaskCompleted(taskEntity, commandContext);

        CommandContextUtil.getInternalTaskAssignmentManager(commandContext).addUserIdentityLinkToParent(taskEntity, Authentication.getAuthenticatedUserId());
        CommandContextUtil.getCmmnEngineConfiguration(commandContext).getListenerNotificationHelper().executeTaskListeners(taskEntity, TaskListener.EVENTNAME_COMPLETE);

        if (planItemInstanceEntity != null) {
            CommandContextUtil.getAgenda(commandContext).planTriggerPlanItemInstanceOperation(planItemInstanceEntity);
        } else {
            TaskHelper.deleteTask(taskEntity, null, false, true);
        }
        
        return null;
    }

    protected static void logTaskCompleted(TaskEntity taskEntity, CommandContext commandContext) {
        if (CommandContextUtil.getTaskServiceConfiguration(commandContext).isEnableDatabaseEventLogging()) {
            TaskLogEntryEntity taskLogEntry = org.flowable.task.service.impl.util.CommandContextUtil.getTaskLogEntryEntityManager().create();
            taskLogEntry.setTaskId(taskEntity.getId());
            taskLogEntry.setSubScopeId(taskEntity.getSubScopeId());
            taskLogEntry.setScopeType(taskEntity.getScopeType());
            taskLogEntry.setScopeId(taskEntity.getScopeId());
            taskLogEntry.setScopeDefinitionId(taskEntity.getScopeDefinitionId());
            taskLogEntry.setTimeStamp(CommandContextUtil.getTaskServiceConfiguration(commandContext).getClock().getCurrentTime());
            taskLogEntry.setType(FlowableEngineEventType.TASK_COMPLETED.name());
            taskLogEntry.setUserId(Authentication.getAuthenticatedUserId());
            CommandContextUtil.getTaskService().addTaskLogEntry(taskLogEntry);
        }
    }

}
