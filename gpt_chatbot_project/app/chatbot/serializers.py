from rest_framework import serializers
from .models import UserProfile   # ← 모델명 맞춰서 import안하면 오류납니다..

class RegisterSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserProfile
        fields = ('username', 'password', 'personality')
        extra_kwargs = {
            'password': {'write_only': True}
        }

    def create(self, validated_data):
        user = UserProfile (
            username=validated_data['username'],
            personality=validated_data.get('personality', '')
        )
        user.set_password(validated_data['password'])
        user.save()
        return user
