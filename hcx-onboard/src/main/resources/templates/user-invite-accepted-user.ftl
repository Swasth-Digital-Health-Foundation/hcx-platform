<html>
<body>
Dear ${USER_NAME},<br/><br/>
Congratulations! We are pleased to inform you that you have successfully completed the user onboarding process for role <strong>${ROLE}</strong> for <strong>${PARTICIPANT_NAME}</strong> on the HCX platform.<br/><br/>
Your user_id will be : ${USER_ID} <br/><br/>
<#if env == "poc">
   Please use your assigned username and password to log in at the following link: <a href="https://hcx.swasth.app/onboarding/login">SSP-Login</a>.<br/><br/>
<#else>
   Please use your assigned username and password to log in at the following link: <a href="https://${ENV}-hcx.swasth.app/onboarding/login">SSP-Login</a>.<br/><br/>
</#if>
If you have any questions or need assistance, please sign up at <a href="https://swasth.zohodesk.in/portal/en/signin">swasth-zohodesk</a> and raise a ticket to contact our support team.
We wish you the best of luck in your new role.<br/><br/>
Best regards,<br/>
HCX Onboarding Team
</body>
</html>