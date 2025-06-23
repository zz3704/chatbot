from django.urls import path
from .views import chat_with_gpt
from django.urls import path
from .views import register, login

urlpatterns = [
    path('chat/', chat_with_gpt),
    path('register/', register),
    path('login/', login),
]
