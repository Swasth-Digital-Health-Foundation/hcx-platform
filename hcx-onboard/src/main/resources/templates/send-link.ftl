<html>
<body>
Hi ${USER_NAME},<br/><br/>
Your Participant Code is <b>${PARTICIPANT_CODE}</b><#if USER_ID != ""> and Root Admin User Id is <b>${USER_ID}</b></#if>.<br/><br/>
Please click on the <a href= ${URL}>link</a> to confirm your email address.<br/><br/>
The link will expire within 7 days, Please regenerate the link, if it is expired.<br/><br/>
<#if role == "payor">
   <b>Note:</b>For payors, to complete the identity verification, Please reach out to HCX Team.
   <br/><br/>
</#if>
 <a href= ${TERMS_AND_CONDITIONS_URL}>Terms and conditions</a><br/><br/>
Thanks,<br/>
HCX Team.
</body>
</html>
