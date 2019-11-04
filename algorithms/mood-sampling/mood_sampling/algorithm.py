class MoodSampling():
    def produce_mood (location):
        from haversine import haversine
        HOME = 42.2972584,-83.7249049
        CLOSE_ENOUGH = 0.1 # km

        lat, lng, speed = location
        if haversine(HOME, (lat, lng)) < CLOSE_ENOUGH:
            # send text
            # from twilio.rest import Client
            # import os

            # # Your Account Sid and Auth Token from twilio.com/console
            # account_sid = os.environ['TWILIO_ACCOUNT_SID']
            # auth_token = os.environ['TWILIO_AUTH_TOKEN']

            # client = Client(account_sid, auth_token)

            # # message = client.messages \
            # #                 .create(
            # #                     body="How was your day? Enter 1 - 7 (1 being the worst)",
            # #                     from_='+17344363993',
            # #                     to='+17343584745'
            # #                 )

            # # message = client.messages.create(
            # #                           body='Hello there!',
            # #                           from_='+17344363993',
            # #                           media_url=['https://demo.twilio.com/owl.png'],
            # #                           to='+17343584745'
            # #                       )
            
            # print(message.sid)

            
