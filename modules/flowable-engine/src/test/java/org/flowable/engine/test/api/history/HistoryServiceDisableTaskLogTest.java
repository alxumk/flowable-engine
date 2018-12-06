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
package org.flowable.engine.test.api.history;

import static org.assertj.core.api.Assertions.assertThat;

import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.test.ConfigurationResource;
import org.flowable.engine.test.FlowableTest;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * @author martin.grofcik
 */
@FlowableTest
@ConfigurationResource("flowable.disable-usertask-log.cfg.xml")
public class HistoryServiceDisableTaskLogTest {

    protected Task task;

    @AfterEach
    public void deleteTasks(TaskService taskService, HistoryService historyService) {
        if (task != null) {
            assertThat(taskService.createTaskLogEntryQuery().count()).isEqualTo(0l);
            taskService.deleteTask(task.getId(), true);
        }
    }

    @Test
    public void createTaskEvent(TaskService taskService) {
        task = taskService.createTaskBuilder().
            assignee("testAssignee").
            create();
    }

    @Test
    public void createTaskEventAsAuthenticatedUser(TaskService taskService) {
        String previousUserId = Authentication.getAuthenticatedUserId();
        Authentication.setAuthenticatedUserId("testUser");
        try {
            task = taskService.createTaskBuilder().
                assignee("testAssignee").
                create();
        } finally {
            Authentication.setAuthenticatedUserId(previousUserId);
        }
    }

}
