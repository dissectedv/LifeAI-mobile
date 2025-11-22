from rest_framework import serializers
from .models import (
    PerfilUsuario,
    RegistroCorporal,
    Dieta,
    Compromisso,
    PontuacaoCompromisso,
    CompromissoAtividade,
    ComposicaoCorporal,
    HistoricoExercicio
)
from datetime import date

class PerfilUsuarioSerializer(serializers.ModelSerializer):
    class Meta:
        model = PerfilUsuario
        exclude = ['id_usuario']

class RegistroCorporalSerializer(serializers.ModelSerializer):
    class Meta:
        model = RegistroCorporal
        fields = ['id', 'data_consulta', 'peso', 'altura', 'imc_res', 'classificacao']
        read_only_fields = ['imc_res', 'classificacao']

    def validate_data_consulta(self, value):
        user = self.context['request'].user
        if value > date.today():
            raise serializers.ValidationError("Data não pode ser no futuro.")
        return value

    def create(self, validated_data):
        validated_data['id_usuario'] = self.context['request'].user
        return super().create(validated_data)

class GraficoEvolucaoSerializer(serializers.ModelSerializer):
    class Meta:
        model = RegistroCorporal
        fields = ['data_consulta', 'imc_res']

class DietaSerializer(serializers.ModelSerializer):
    class Meta:
        model = Dieta
        fields = ['id', 'data_criacao', 'plano_alimentar']

class ComposicaoCorporalSerializer(serializers.ModelSerializer):
    class Meta:
        model = ComposicaoCorporal
        fields = [
            'id', 'data_consulta', 'gordura_percentual', 'musculo_percentual',
            'agua_percentual', 'gordura_visceral', 'estimado'
        ]
        read_only_fields = ['data_consulta']

    def create(self, validated_data):
        validated_data['id_usuario'] = self.context['request'].user
        validated_data['data_consulta'] = date.today()
        return super().create(validated_data)

class AtividadeSerializer(serializers.ModelSerializer):
    class Meta:
        model = CompromissoAtividade
        fields = ['id', 'descricao', 'done']

class CompromissoSerializer(serializers.ModelSerializer):
    class Meta:
        model = Compromisso
        fields = ['id', 'titulo', 'data', 'hora_inicio', 'hora_fim', 'concluido']

    def create(self, validated_data):
        validated_data['id_usuario'] = self.context['request'].user
        return super().create(validated_data)

class PontuacaoCompromissoSerializer(serializers.ModelSerializer):
    data_compromisso = serializers.DateField(source='compromisso.data', format='%Y-%m-%d')
    compromisso_id = serializers.IntegerField(source='compromisso.id', read_only=True)

    class Meta:
        model = PontuacaoCompromisso
        fields = [
            'id', 'compromisso_id', 'qtd_total_atv', 'qtd_atv_done',
            'porcentagem', 'criado_em', 'data_compromisso'
        ]

class HistoricoExercicioSerializer(serializers.ModelSerializer):
    exercise_name = serializers.CharField(source='nome_exercicio')
    duration_seconds = serializers.IntegerField(source='duracao_segundos')
    calories_burned = serializers.IntegerField(source='calorias_queimadas')
    created_at = serializers.DateTimeField(source='data_treino')

    class Meta:
        model = HistoricoExercicio
        fields = ['id', 'exercise_name', 'duration_seconds', 'calories_burned', 'created_at']

    def create(self, validated_data):
        validated_data['id_usuario'] = self.context['request'].user
        return super().create(validated_data)