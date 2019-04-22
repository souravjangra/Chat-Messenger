import smtplib 
  
# creates SMTP session 
s = smtplib.SMTP('smtp.gmail.com', 587) 
  
# start TLS for security 
s.starttls() 
  
# Authentication 

try:
	s.login("apjasuian@gmail.com", "asu@12345")# sender email and password
	# message to be sent 
	message =open('new.txt').read()
	print(message)
	# sending the mail 
	BODY = '\r\n'.join(['To: %s' %"imsouravjangra@gmail.com" ,'From: %s' % "apjasuian@gmail.com",'Subject: %s' % "chats archieved",'', message])
	s.sendmail("sender_email_id", "imsouravjangra@gmail.com", BODY)
except:
	print('error occured')