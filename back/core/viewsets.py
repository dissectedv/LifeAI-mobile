from django.contrib.auth.models import User
from django.contrib.auth import authenticate, logout
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status, permissions
from core import serializers
from core import models
from rest_framework_simplejwt.tokens import RefreshToken
from datetime import date

from core.welcome_email_html.welcome import send_welcome_email_html

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

        try:
            send_welcome_email_html(user)
        except Exception as e:
            print(f"ALERTA: Falha ao enviar e-mail: {str(e)}")

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

        onboarding_completed = hasattr(user, 'perfil')

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
        return Response({'status': 'Email functionality placeholder'})

class PerfilUsuarioView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        try:
            perfil = request.user.perfil
            serializer = serializers.PerfilUsuarioSerializer(perfil)
            return Response(serializer.data, status=status.HTTP_200_OK)
        except models.PerfilUsuario.DoesNotExist:
            return Response({"erro": "Perfil não encontrado. Complete seu cadastro."}, status=status.HTTP_404_NOT_FOUND)

    def post(self, request):
        if hasattr(request.user, 'perfil'):
            return Response({"erro": "Este usuário já possui um perfil."}, status=status.HTTP_400_BAD_REQUEST)

        serializer = serializers.PerfilUsuarioSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save(id_usuario=request.user)
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def patch(self, request):
        try:
            perfil = request.user.perfil
        except models.PerfilUsuario.DoesNotExist:
            return Response({"erro": "Perfil não encontrado."}, status=status.HTTP_404_NOT_FOUND)

        serializer = serializers.PerfilUsuarioSerializer(perfil, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_200_OK)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class RegistroCorporalCreateView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):
        data = request.data.copy()
        if 'data_consulta' not in data:
            data['data_consulta'] = date.today()

        serializer = serializers.RegistroCorporalSerializer(data=data, context={'request': request})

        if serializer.is_valid():
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

            registro = serializer.save(
                imc_res=imc_valor,
                classificacao=classificacao
            )

            return Response({
                'mensagem': 'Registro salvo com sucesso!',
                'id': registro.id,
                'imc': imc_valor,
                'classificacao': classificacao
            }, status=status.HTTP_201_CREATED)

        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class HistoricoRegistrosView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        registros = models.RegistroCorporal.objects.filter(id_usuario=request.user).order_by('-data_consulta')
        serializer = serializers.RegistroCorporalSerializer(registros, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


class GraficoEvolucaoView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        registros = models.RegistroCorporal.objects.filter(id_usuario=request.user).order_by('data_consulta')
        serializer = serializers.GraficoEvolucaoSerializer(registros, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


class RegistroDeleteView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def delete(self, request, pk):
        try:
            registro = models.RegistroCorporal.objects.get(id=pk, id_usuario=request.user)
            registro.delete()
            return Response(status=status.HTTP_204_NO_CONTENT)
        except models.RegistroCorporal.DoesNotExist:
            return Response({"error": "Registro não encontrado."}, status=status.HTTP_404_NOT_FOUND)


class HistoricoDietasView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        dietas = models.Dieta.objects.filter(id_usuario=request.user).order_by('-data_criacao')
        serializer = serializers.DietaSerializer(dietas, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


class ComposicaoCorporalListCreateAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        registros = models.ComposicaoCorporal.objects.filter(id_usuario=request.user).order_by('-data_consulta')
        serializer = serializers.ComposicaoCorporalSerializer(registros, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

    def post(self, request):
        serializer = serializers.ComposicaoCorporalSerializer(data=request.data, context={'request': request})
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class CompromissosListCreateAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        compromissos = models.Compromisso.objects.filter(id_usuario=request.user).order_by('-data', 'hora_inicio')
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
            return models.Compromisso.objects.get(id=pk, id_usuario=user)
        except models.Compromisso.DoesNotExist:
            return None

    def get(self, request, pk):
        compromisso = self.get_object(pk, request.user)
        if not compromisso:
            return Response({'erro': 'Compromisso não encontrado.'}, status=status.HTTP_404_NOT_FOUND)
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
        serializer = serializers.CompromissoSerializer(compromisso, data=request.data, partial=True,
                                                       context={'request': request})
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
            comp = models.Compromisso.objects.get(id=compromisso_id, id_usuario=request.user)
        except models.Compromisso.DoesNotExist:
            return Response({'erro': 'Compromisso não encontrado'}, status=status.HTTP_404_NOT_FOUND)

        atividades = models.CompromissoAtividade.objects.filter(compromisso=comp)
        total = atividades.count()
        feitas = atividades.filter(done=True).count()

        if total == 0:
            return Response({'erro': 'Compromisso sem atividades'}, status=status.HTTP_400_BAD_REQUEST)

        porcentagem = (feitas / total) * 100

        pontuacao, _ = models.PontuacaoCompromisso.objects.update_or_create(
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

        pontuacoes = models.PontuacaoCompromisso.objects.filter(
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

class HistoricoExercicioView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        historico = models.HistoricoExercicio.objects.filter(id_usuario=request.user).order_by('-data_treino')
        serializer = serializers.HistoricoExercicioSerializer(historico, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

    def post(self, request):
        serializer = serializers.HistoricoExercicioSerializer(data=request.data, context={'request': request})
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)