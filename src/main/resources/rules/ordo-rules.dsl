rule TaskCreatedAuditRule
when #eventType == "TASK_CREATED"
then ruleActions.logTaskCreated(#taskId, #taskTitle)

rule TaskCreatedUrgentRule
when #eventType == "TASK_CREATED"
then ruleActions.flagUrgentTask(#taskId, #cardId, #taskTitle)

rule TaskCreatedNotifyRule
when #eventType == "TASK_CREATED"
then ruleActions.notifyTaskRuleTriggered(#cardId, "TaskCreatedNotifyRule", #taskTitle)

rule TaskUpdatedAuditRule
when #eventType == "TASK_UPDATED"
then ruleActions.logTaskUpdated(#taskId, #taskTitle, #taskCompleted)

rule TaskUpdatedCompletedNotifyRule
when #eventType == "TASK_UPDATED" and #taskCompleted == true
then ruleActions.notifyTaskRuleTriggered(#cardId, "TaskUpdatedCompletedNotifyRule", #taskTitle)

rule TaskUpdatedUrgentNotifyRule
when #eventType == "TASK_UPDATED" and #taskTitle != null and #taskTitle.toUpperCase().startsWith("URGENT")
then ruleActions.notifyTaskRuleTriggered(#cardId, "TaskUpdatedUrgentNotifyRule", #taskTitle)

rule CardCreatedAuditRule
when #eventType == "CARD_CREATED"
then ruleActions.logCardCreated(#cardId, #cardTitle)

rule CardCreatedNotifyRule
when #eventType == "CARD_CREATED"
then ruleActions.notifyCardRuleTriggered(#boardId, "CardCreatedNotifyRule", #cardTitle)

rule CardCreatedUrgentAuditRule
when #eventType == "CARD_CREATED" and #cardTitle != null and #cardTitle.toUpperCase().startsWith("URGENT")
then ruleActions.notifyCardRuleTriggered(#boardId, "CardCreatedUrgentAuditRule", #cardTitle)
