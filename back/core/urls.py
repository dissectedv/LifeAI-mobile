from django.urls import path
from core import viewsets
from core import views
from .services import api_ia
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView

urlpatterns = [
    path('registro/', viewsets.RegisterView.as_view(), name='RegisterView'),
    path('login/', viewsets.LoginView.as_view(), name='LoginView'),
    path('logout/', viewsets.LogoutView.as_view(), name='logout'),
    path('send-email/', viewsets.SendEmailView.as_view(), name='email'),
    path('api/token', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('api/token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('perfil/', viewsets.PerfilUsuarioView.as_view(), name='perfil_usuario'),
    path('imc/', viewsets.RegistroCorporalCreateView.as_view(), name='criar_registro_imc'),
    path('imc/historico/', viewsets.GraficoEvolucaoView.as_view(), name='grafico_evolucao'),
    path('imc/registrosConsultas/', viewsets.HistoricoRegistrosView.as_view(), name='historico_tabela'),
    path('imc/<int:pk>/', viewsets.RegistroDeleteView.as_view(), name='deletar_registro'),
    path('composicao-corporal/', viewsets.ComposicaoCorporalListCreateAPIView.as_view(), name='composicao_corporal'),
    path('gerar-dieta-ia/', api_ia.gerar_dieta_ia_view, name='gerar-dieta-ia'),
    path('chat-ia/', api_ia.chat_ia_view, name='chat-ia'),
    path('dietas/historico/', viewsets.HistoricoDietasView.as_view(), name='historico_dietas'),
    path('compromissos/', viewsets.CompromissosListCreateAPIView.as_view(), name='compromissos-list-create'),
    path('compromissos/<int:pk>/', viewsets.CompromissoRetrieveUpdateDeleteAPIView.as_view(), name='compromisso-crud'),
    path('pontuacoes/mensal/', viewsets.DesempenhoMensalAPIView.as_view(), name='pontuacoes-mensal'),
    path('compromissos/<int:compromisso_id>/pontuacao/', viewsets.GerarPontuacaoCompromissoAPIView.as_view(), name='gerar_pontuacao'),
    path('health/', views.health_check, name="health_check"),
    path('exercicios/', viewsets.HistoricoExercicioView.as_view(), name='historico_exercicios'),
]