<html>
<body>
Hi ${USER_NAME},<br/><br/>
Your participant code is <b>${PARTICIPANT_CODE}</b>.<br/><br/>
<a href= ${URL}>Link to verify your email address</a> <br/><br/>
This link will expire within ${DAY} days.<br/><br/>
<#if role == "payor">
   <b>Note:</b> To complete the Identity Verification, Please reach out to HCX Team.
   <br/><br/>
</#if>
Thanks,<br/>
HCX Team.
</body>
</html>
