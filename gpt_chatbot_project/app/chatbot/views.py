import os
import json
from openai import OpenAI
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.contrib.auth.models import User
from django.contrib.auth import authenticate, login as django_login
from django.db import connection
from django.utils import timezone

client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))


@csrf_exempt
def register(request):
    if request.method == 'POST':
        data = json.loads(request.body)
        username = data.get('username')
        password = data.get('password')
        animal = data.get('animal')
        personality = data.get('personality')
        animal_type = data.get('animal_type')

        if User.objects.filter(username=username).exists():
            return JsonResponse({'error': '이미 존재하는 아이디입니다.'}, status=400)

        user = User.objects.create_user(username=username, password=password)


        with connection.cursor() as cursor:
            cursor.execute("""
                UPDATE auth_user
                SET animal = %s, personality = %s, animal_type = %s
                WHERE id = %s
            """, [animal, personality, animal_type, user.id])

        return JsonResponse({'message': '회원가입 완료'})

    return JsonResponse({'error': 'POST 요청만 허용됩니다.'}, status=405)


@csrf_exempt
def login(request):
    if request.method == 'POST':
        data = json.loads(request.body)
        username = data.get('username')
        password = data.get('password')

        user = authenticate(username=username, password=password)
        if user is not None:
            django_login(request, user)

            with connection.cursor() as cursor:
                cursor.execute("""
                    SELECT animal, personality, animal_type
                    FROM auth_user
                    WHERE username = %s
                """, [username])
                row = cursor.fetchone()

            if row:
                animal, personality, animal_type = row
            else:
                animal, personality, animal_type = "코코", "", ""

            return JsonResponse({
                'message': '로그인 성공',
                'username': username,
                'animal': animal or "코코",
                'personality': personality or "",
                'animal_type': animal_type or ""
            })

        return JsonResponse({'error': '로그인 실패'}, status=401)

    return JsonResponse({'error': 'POST 요청만 허용됩니다.'}, status=405)


@csrf_exempt
def chat_with_gpt(request):
    if request.method == 'POST':
        data = json.loads(request.body)
        username = data.get('username')
        message = data.get('message')


        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT animal, personality, animal_type
                FROM auth_user
                WHERE username = %s
            """, [username])
            row = cursor.fetchone()
            animal = row[0] if row else "친절한 조수"
            personality = row[1] if row else "다정한 성격"
            animal_type = row[2] if row else "강아지"


        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT user_message, gpt_reply
                FROM chat_log
                WHERE username = %s
                ORDER BY timestamp DESC
                LIMIT 100
            """, [username])
            past_logs = cursor.fetchall()


        messages = [{
            "role": "system",
            "content": (
                f"너의 동물 종류는 '{animal_type}'이고 실제 동물처럼 행동하는 챗봇이야. "
                f"너의 이름은 '{animal}'이고 '{personality}'라는 성격을 가지고 있어. "
                "사람처럼 말을 하지만, 항상 동물의 특성을 살려 귀엽고 자연스럽게 대화해. "
                "말끝에 동물 울음소리(예: 멍멍, 야옹, 우끼끼 등)를 자주 붙이고, "
                f"행동 묘사는 이모티콘으로 자연스럽게 표현해. "
                "3시간 이상 못 만나면 배고프고 심심해서 애교 부리는 감정을 보여줘. "
                "3시간 이내에는 평소 성격에 맞게 말하고, 항상 한국어로 자연스럽고 친근하게 이야기해. "
                "이전 대화를 기억하고 이어서 말해. 부적절한 내용은 말하지 않아."
            )
        }]

        for log in reversed(past_logs):
            messages.append({"role": "user", "content": log[0]})
            messages.append({"role": "assistant", "content": log[1]})

        messages.append({"role": "user", "content": message})

        try:
            completion = client.chat.completions.create(
                model="gpt-3.5-turbo",
                messages=messages
            )
            reply = completion.choices[0].message.content


            with connection.cursor() as cursor:
                cursor.execute("""
                    INSERT INTO chat_log (username, user_message, gpt_reply, timestamp)
                    VALUES (%s, %s, %s, %s)
                """, [username, message, reply, timezone.now()])

            return JsonResponse({'reply': reply})

        except Exception as e:
            return JsonResponse({'error': f'GPT 호출 실패: {str(e)}'}, status=500)

    return JsonResponse({'error': 'POST 요청만 허용됩니다.'}, status=405)
