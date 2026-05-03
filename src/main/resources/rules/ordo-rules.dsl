rule TaskCreatedAuditRule
when #eventType == "TASK_CREATED"
then ordoRuleActions.logTaskCreated(#taskId, #taskTitle)

rule TaskCreatedUrgentRule
when #eventType == "TASK_CREATED"
then ordoRuleActions.flagUrgentTask(#taskId, #cardId, #taskTitle)
