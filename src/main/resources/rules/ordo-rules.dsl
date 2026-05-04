rule TaskCreatedAuditRule
when #eventType == "TASK_CREATED"
then ordoRuleActions.logTaskCreated(#taskId, #taskTitle)

rule TaskCreatedUrgentRule
when #eventType == "TASK_CREATED"
then ordoRuleActions.flagUrgentTask(#taskId, #cardId, #taskTitle)

rule TaskCreatedNotifyRule
when #eventType == "TASK_CREATED"
then ordoRuleActions.notifyTaskRuleTriggered(#cardId, "TaskCreatedNotifyRule", #taskTitle)

rule TaskUpdatedAuditRule
when #eventType == "TASK_UPDATED"
then ordoRuleActions.logTaskUpdated(#taskId, #taskTitle, #taskCompleted)

rule CardCreatedAuditRule
when #eventType == "CARD_CREATED"
then ordoRuleActions.logCardCreated(#cardId, #cardTitle)

rule CardCreatedNotifyRule
when #eventType == "CARD_CREATED"
then ordoRuleActions.notifyCardRuleTriggered(#boardId, "CardCreatedNotifyRule", #cardTitle)
