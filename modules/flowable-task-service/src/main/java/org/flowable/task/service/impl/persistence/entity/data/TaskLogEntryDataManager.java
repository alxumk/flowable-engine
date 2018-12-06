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
package org.flowable.task.service.impl.persistence.entity.data;

import java.util.List;
import java.util.Map;

import org.flowable.common.engine.impl.persistence.entity.data.DataManager;
import org.flowable.task.api.TaskLogEntry;
import org.flowable.task.service.impl.TaskLogEntryQueryImpl;
import org.flowable.task.service.impl.persistence.entity.TaskLogEntryEntity;

/**
 * author martin.grofcik
 */
public interface TaskLogEntryDataManager extends DataManager<TaskLogEntryEntity> {

    void deleteTaskLogEntry(long logEntryNumber);

    long findTaskLogEntriesCountByQueryCriteria(TaskLogEntryQueryImpl taskLogEntryQuery);

    List<TaskLogEntry> findTaskLogEntriesByQueryCriteria(TaskLogEntryQueryImpl taskLogEntryQuery);

    long findTaskLogEntriesCountByNativeQueryCriteria(Map<String, Object> nativeTaskLogEntryQuery);

    List<TaskLogEntry> findTaskLogEntriesByNativeQueryCriteria(Map<String, Object> nativeTaskLogEntryQuery);

    void deleteTaskLogEntriesByProcessDefinitionId(String processDefinitionId);

    void deleteTaskLogEntriesByScopeDefinitionId(String scopeType, String scopeDefinitionId);

    void deleteTaskLogEntriesByTaskId(String taskId);
}
