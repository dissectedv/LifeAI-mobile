from django.contrib.auth.models import User
from django.contrib.auth import authenticate, logout
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status, permissions
from core import serializers
from core import models
from rest_framework_simplejwt.tokens import RefreshToken
from datetime import datetime

from django.conf import settings
from django.core.mail import EmailMultiAlternatives
from rest_framework import status

class RegisterView(APIView):
    permission_classes = [permissions.AllowAny]

    def post(self, request):
        data = request.data
        username = data.get('username')
        email = data.get('email')
        password = data.get('password')

        if not username or not email or not password:
            return Response({'message': 'Todos os campos são obrigatórios.'}, status=400)

        if User.objects.filter(username=username).exists():
            return Response({'message': 'Nome de usuário já em uso.'}, status=400)

        if User.objects.filter(email=email).exists():
            return Response({'message': 'Email já em uso.'}, status=400)

        user = User.objects.create_user(username=username, email=email, password=password)
        refresh = RefreshToken.for_user(user)

        return Response({
            'message': 'Usuário criado com sucesso.',
            'refresh': str(refresh),
            'access': str(refresh.access_token),
            'user': {
                'id': user.id,
                'username': user.username,
                'email': user.email
            }
        }, status=201)

class LoginView(APIView):
    permission_classes = [permissions.AllowAny]

    def post(self, request):
        email = request.data.get("email")
        password = request.data.get("password")

        if not email or not password:
            return Response({"error": "Email e senha são obrigatórios."}, status=status.HTTP_400_BAD_REQUEST)

        try:
            user = User.objects.get(email=email)
        except User.DoesNotExist:
            return Response({"error": "Usuário com esse email não encontrado."}, status=status.HTTP_404_NOT_FOUND)

        user = authenticate(username=user.username, password=password)
        onboarding_completed = models.imc_user_base.objects.filter(id_usuario=user).exists()

        if user is not None:
            refresh = RefreshToken.for_user(user)
            return Response({
                'message': 'Login bem-sucedido',
                'access': str(refresh.access_token),
                'refresh': str(refresh),
                'user_id': user.id,
                'username': user.username,
                'email': user.email,
                'onboarding_completed': onboarding_completed
            })
        else:
            return Response({'message': 'Credenciais inválidas'}, status=401)

class LogoutView(APIView):
    permission_classes = [permissions.IsAuthenticated]
    
    def post(self, request):
        logout(request)
        return Response({"message": "Logout realizado com sucesso."}, status=status.HTTP_200_OK)

class SendEmailView(APIView):
    def post(self, request):
        subject = request.data.get('subject')
        message = request.data.get('message')
        recipient = request.data.get('to')
        html_content = request.data.get('html')

        if not all([subject, message, recipient]):
            return Response({'error': 'Campos obrigatórios ausentes'}, status=status.HTTP_400_BAD_REQUEST)

        from_email = getattr(settings, 'DEFAULT_FROM_EMAIL', settings.EMAIL_HOST_USER)

        try:
            msg = EmailMultiAlternatives(
                subject=subject,
                body=message,
                from_email=from_email,
                to=[recipient],
            )
            if html_content:
                msg.attach_alternative(html_content, "text/html")
            msg.send(fail_silently=False)
            return Response({'success': 'E-mail enviado com sucesso'})
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

class ImcCreateAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):
        serializer = serializers.ImcSerializer(data=request.data, context={'request': request})
        if serializer.is_valid():
            data_consulta = serializer.validated_data['data_consulta']
            idade = serializer.validated_data['idade']
            sexo = serializer.validated_data['sexo']
            peso = serializer.validated_data['peso']
            altura = serializer.validated_data['altura']
            imc_valor = peso / (altura ** 2)

            if imc_valor < 18.5:
                classificacao = "Abaixo do peso"
            elif 18.5 <= imc_valor < 25:
                classificacao = "Peso normal"
            elif 25 <= imc_valor < 30:
                classificacao = "Sobrepeso"
            else:
                classificacao = "Obesidade"

            imc_obj = serializer.save(
                id_usuario=request.user,
                data_consulta=data_consulta,
                idade=idade,
                sexo=sexo,
                peso=peso,
                altura=altura,
                imc_res=imc_valor,
                classificacao=classificacao
            )
            return Response({
                'mensagem': 'IMC registrado com sucesso.',
                'id': imc_obj.id,
                'imc': imc_valor,
                'classificacao': classificacao
            }, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class DesempenhoImc(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        registros = serializers.imc.objects.filter(id_usuario=request.user).order_by('-id')
        serializer = serializers.DesempenhoImcGrafico(registros, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

class RegistrosImc(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        registros = serializers.imc.objects.filter(id_usuario=request.user).order_by('-data_consulta')
        serializer = serializers.ImcSerializer(registros, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

class ImcBaseAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):
        serializer = serializers.ImcBaseSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save(id_usuario=request.user)
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class ImcBaseDashAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        registros = serializers.imc_user_base.objects.filter(id_usuario=request.user).order_by('-id')
        serializer = serializers.ImcBaseSerializer(registros, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

class ImcBaseRecAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        registros = serializers.imc_user_base.objects.filter(id_usuario=request.user).order_by('-id')
        serializer = serializers.ImcBaseRecSerializer(registros, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

class ImcDeleteAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def delete(self, request, pk):
        try:
            registro = serializers.imc.objects.get(id=pk, id_usuario=request.user)
        except serializers.imc.DoesNotExist:
            return Response({"error": "Registro não encontrado."}, status=status.HTTP_404_NOT_FOUND)
        registro.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)
    
class CompromissosListCreateAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        compromissos = models.compromisso.objects.filter(id_usuario=request.user).order_by('-data', 'hora_inicio')
        serializer = serializers.CompromissoSerializer(compromissos, many=True)
        return Response(serializer.data)

    def post(self, request):
        serializer = serializers.CompromissoSerializer(data=request.data, context={'request': request})
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
class CompromissoRetrieveUpdateDeleteAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get_object(self, pk, user):
        try:
            return models.compromisso.objects.get(id=pk, id_usuario=user)
        except models.compromisso.DoesNotExist:
            return None

    def get(self, request, pk):
        compromisso = self.get_object(pk, request.user)
        if not compromisso:
            return Response({'erro': 'Compromisso não encontrado.'}, status=status.HTTP_44_NOT_FOUND)
        serializer = serializers.CompromissoSerializer(compromisso)
        return Response(serializer.data)

    def put(self, request, pk):
        compromisso = self.get_object(pk, request.user)
        if not compromisso:
            return Response({'erro': 'Compromisso não encontrado.'}, status=status.HTTP_404_NOT_FOUND)
        serializer = serializers.CompromissoSerializer(compromisso, data=request.data, context={'request': request})
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def patch(self, request, pk):
        compromisso = self.get_object(pk, request.user)
        if not compromisso:
            return Response({'erro': 'Compromisso não encontrado.'}, status=status.HTTP_404_NOT_FOUND)
        serializer = serializers.CompromissoSerializer(compromisso, data=request.data, partial=True, context={'request': request})
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def delete(self, request, pk):
        compromisso = self.get_object(pk, request.user)
        if not compromisso:
            return Response({'erro': 'Compromisso não encontrado.'}, status=status.HTTP_404_NOT_FOUND)
        compromisso.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)

class GerarPontuacaoCompromissoAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request, compromisso_id):
        try:
            comp = models.compromisso.objects.get(id=compromisso_id, id_usuario=request.user)
        except models.compromisso.DoesNotExist:
            return Response({'erro': 'Compromisso não encontrado'}, status=status.HTTP_404_NOT_FOUND)

        atividades = models.atividade.objects.filter(compromisso=comp)
        total = atividades.count()
        feitas = atividades.filter(done=True).count()

        if total == 0:
            return Response({'erro': 'Compromisso sem atividades'}, status=status.HTTP_400_BAD_REQUEST)

        porcentagem = (feitas / total) * 100

        pontuacao, _ = models.pontuacao_compromisso.objects.update_or_create(
            compromisso=comp,
            defaults={
                'qtd_total_atv': total,
                'qtd_atv_done': feitas,
                'porcentagem': porcentagem
            }
        )

        return Response({
            'compromisso_id': comp.id,
            'qtd_total_atv': total,
            'qtd_atv_done': feitas,
            'porcentagem': round(pontuacao.porcentagem, 2)
        })
    
class DesempenhoMensalAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        ano = request.GET.get('ano')
        mes = request.GET.get('mes')

        if not ano or not mes:
            return Response({"erro": "Ano e mês são obrigatórios."}, status=400)

        try:
            ano = int(ano)
            mes = int(mes)
        except ValueError:
            return Response({"erro": "Ano e mês devem ser números."}, status=400)

        pontuacoes = models.pontuacao_compromisso.objects.filter(
            compromisso__id_usuario=request.user,
            compromisso__data__year=ano,
            compromisso__data__month=mes
        ).values(
            'compromisso__data',
            'porcentagem'
        )

        resultado = []
        for p in pontuacoes:
            data_str = p['compromisso__data'].strftime('%Y-%m-%d')
            porcentagem = p['porcentagem']
            if porcentagem < 33:
                emoji = '🧊'
            elif porcentagem < 66:
                emoji = '😐'
            else:
                emoji = '🔥'

            resultado.append({
                'data': data_str,
                'emoji': emoji,
                'porcentagem': porcentagem
            })

        return Response(resultado)