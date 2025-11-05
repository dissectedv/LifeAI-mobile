from django.urls import path
from core import viewsets
from core import views
from core.services.api_ia import chat_ia_view
from .services import api_ia
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView

urlpatterns = [
    path('registro/', viewsets.RegisterView.as_view(), name='RegisterView'),
    path('login/', viewsets.LoginView.as_view(), name='LoginView'),
    path('logout/', viewsets.LogoutView.as_view(), name='logout'),
    path('imc/', viewsets.ImcCreateAPIView.as_view(), name='criar_imc'),
    path('imc/historico/', viewsets.DesempenhoImc.as_view(), name='desempenho-imc'),
    path('imc/registrosConsultas/', viewsets.RegistrosImc.as_view(), name='registros_imc'),
    path('imc/<int:pk>/', viewsets.ImcDeleteAPIView.as_view(), name='deletar_imc'),
    path('chat-ia/', chat_ia_view, name="chat-ia"),
    path('api/token', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('api/token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('imc_base_perfil/', viewsets.ImcBaseAPIView.as_view(), name='imc_base_perfil'),
    path('imc_base_dashboard/', viewsets.ImcBaseDashAPIView.as_view(), name='imc_base_dashboard'),
    path('imc_rec/', viewsets.ImcBaseRecAPIView.as_view(), name='imc_rec'),
    path('checklist/create/', viewsets.ChecklistCreateAPIView.as_view(), name='checklist'),
    path('checklists/atividades-por-data/', viewsets.AtividadesPorDataAPIView.as_view(), name='atividades-por-data'),
    path('checklists/<int:checklist_id>/atualizar-atividades/', viewsets.AtualizarAtividadesAPIView.as_view(), name='atualizar-atividades'),
    path('checklists/<int:checklist_id>/pontuacao/', viewsets.GerarPontuacaoAPIView.as_view()),
    path('pontuacoes/', viewsets.PontuacaoCheckListAPIView.as_view(), name='pontuacoes'),
    path('pontuacoes/mensal/', viewsets.DesempenhoMensalAPIView.as_view(), name='pontuacoes-mensal'),
    path("health/", views.health_check, name="health_check"),
    path('chat-ia/', api_ia.chat_ia_view, name='chat-ia'),
    path('gerar-dieta-ia/', api_ia.gerar_dieta_ia_view, name='gerar-dieta-ia'),
]
