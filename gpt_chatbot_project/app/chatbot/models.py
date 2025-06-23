from django.db import models
from django.contrib.auth.models import User
class ChatLog(models.Model):
    user_message = models.TextField()
    gpt_reply = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)

class UserProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    personality = models.CharField(max_length=255)

    def __str__(self):
        return f"{self.user.username}의 성격: {self.personality}"