from rest_framework import serializers
from .models import imc, imc_user_base, atividade, checklist, pontuacao_check
from datetime import date

class ImcSerializer(serializers.ModelSerializer):
    class Meta:
        model = imc
        fields = ['id', 'data_consulta', 'idade', 'sexo', 'peso', 'altura', 'imc_res', 'classificacao']

    def validate_data_consulta(self, value):
        user = self.context['request'].user
        if value > date.today():
            raise serializers.ValidationError("Data não pode ser no futuro.")
        if self.instance and self.instance.data_consulta == value:
            return value
        if imc.objects.filter(id_usuario=user, data_consulta=value).exists():
            raise serializers.ValidationError("Você já possui um registro com essa data.")
        return value

    def create(self, validated_data):
        validated_data['id_usuario'] = self.context['request'].user
        return super().create(validated_data)

class DesempenhoImcGrafico(serializers.ModelSerializer):
    class Meta:
        model = imc
        fields = ['data_consulta', 'imc_res']

class ImcBaseSerializer(serializers.ModelSerializer):
    class Meta:
        model = imc_user_base
        exclude = ['id_usuario']

class ImcBaseRecSerializer(serializers.ModelSerializer):
    class Meta:
        model = imc_user_base
        fields = ['imc_res', 'objetivo']

class AtividadeSerializer(serializers.ModelSerializer):
    class Meta:
        model = atividade
        fields = ['id', 'descricao', 'done']

class ChecklistSerializer(serializers.ModelSerializer):
    atividades = AtividadeSerializer(many=True, write_only=True)
    tarefas = AtividadeSerializer(source='atividade_set', many=True, read_only=True)

    class Meta:
        model = checklist
        fields = ['data', 'atividades', 'tarefas']
        read_only_fields = ['id', 'created_at']

    def create(self, validated_data):
        atividades_data = validated_data.pop('atividades')
        usuario = self.context['request'].user

        checklist_instance = checklist.objects.create(id_usuario=usuario, **validated_data)

        for atividade_data in atividades_data:
            atividade.objects.create(checklist=checklist_instance, **atividade_data)

        return checklist_instance

class PontuacaoCheckSerializer(serializers.ModelSerializer):
    data_checklist = serializers.DateField(source='checklist.data', format='%Y-%m-%d')
    checklist_id = serializers.IntegerField(source='checklist.id', read_only=True)

    class Meta:
        model = pontuacao_check
        fields = ['id', 'checklist_id', 'qtd_total_atv', 'qtd_atv_done', 'porcentagem', 'criado_em', 'data_checklist']
