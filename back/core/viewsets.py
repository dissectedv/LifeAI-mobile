from django.contrib.auth.models import User
from django.contrib.auth import authenticate, logout
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status, permissions
from core import serializers
from core import models
from rest_framework_simplejwt.tokens import RefreshToken
from datetime import datetime

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
        return Response({"message": "Registro excluído com sucesso."}, status=status.HTTP_204_NO_CONTENT)
    
class ChecklistCreateAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):
        serializer = serializers.ChecklistSerializer(data=request.data, context={'request': request})
        if serializer.is_valid():
            serializer = serializer.save()
            return Response({'message': 'Checklist criado com sucesso.'}, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def buscar_checklist_por_data(request):
        data = request.GET.get('data')
        if not data:
            return Response({'erro': 'Data não fornecida'}, status=400)

        checklist = get_object_or_404(Checklist, id_usuario=request.user, data=data)
        return Response({'id': checklist.id})
    
class AtividadesPorDataAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        data = request.GET.get('data')
        if not data:
            return Response({'erro': 'Parâmetro data é obrigatório.'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            data_convertida = datetime.strptime(data, '%Y-%m-%d').date()
        except ValueError:
            return Response({'erro': 'Formato de data inválido. Use YYYY-MM-DD.'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            checklist_obj = models.checklist.objects.get(id_usuario=request.user, data=data_convertida)
        except models.checklist.DoesNotExist:
            return Response({'mensagem': 'Checklist não encontrado'}, status=status.HTTP_4404_NOT_FOUND)

        atividades = models.atividade.objects.filter(checklist=checklist_obj).order_by('id')
        serializer = serializers.AtividadeSerializer(atividades, many=True)

        return Response({
            'id': checklist_obj.id,
            'data': checklist_obj.data.isoformat(),
            'atividades': serializer.data
        }, status=status.HTTP_200_OK)

class AtualizarAtividadesAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def put(self, request, checklist_id):
        try:
            checklist = models.checklist.objects.get(id=checklist_id, id_usuario=request.user)
        except models.checklist.DoesNotExist:
            return Response({'erro': 'Checklist não encontrado'}, status=status.HTTP_404_NOT_FOUND)

        atividades_data = request.data.get('atividades', [])

        for atv in atividades_data:
            try:
                atividade = models.atividade.objects.get(id=atv['id'], checklist=checklist)
                atividade.done = atv['done']
                atividade.save()
            except models.atividade.DoesNotExist:
                continue

        return Response({'mensagem': 'Atividades atualizadas com sucesso.'}, status=status.HTTP_200_OK)
    
class GerarPontuacaoAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request, checklist_id):
        try:
            check = models.checklist.objects.get(id=checklist_id, id_usuario=request.user)
        except models.checklist.DoesNotExist:
            return Response({'erro': 'Checklist não encontrado'}, status=status.HTTP_4404_NOT_FOUND)

        atividades = models.atividade.objects.filter(checklist=check).order_by('id')
        total = atividades.count()
        feitas = atividades.filter(done=True).count()

        if total == 0:
            return Response({'erro': 'Checklist sem atividades'}, status=status.HTTP_400_BAD_REQUEST)

        porcentagem = (feitas / total) * 100

        pontuacao, _ = models.pontuacao_check.objects.update_or_create(
            checklist=check,
            defaults={
                'qtd_total_atv': total,
                'qtd_atv_done': feitas,
                'porcentagem': porcentagem
            }
        )

        return Response({
            'checklist_id': check.id,
            'qtd_total_atv': total,
            'qtd_atv_done': feitas,
            'porcentagem': round(pontuacao.porcentagem, 2)
        })
    
class PontuacaoCheckListAPIView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        pontuacoes = models.pontuacao_check.objects.filter(checklist__id_usuario=request.user).order_by('checklist__data')

        serializer = serializers.PontuacaoCheckSerializer(pontuacoes, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

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

        pontuacoes = models.pontuacao_check.objects.filter(
            checklist__id_usuario=request.user,
            checklist__data__year=ano,
            checklist__data__month=mes
        ).values(
            'checklist__data',
            'porcentagem'
        )

        resultado = []
        for p in pontuacoes:
            data_str = p['checklist__data'].strftime('%Y-%m-%d')
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