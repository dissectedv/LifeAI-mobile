from rest_framework import serializers
from .models import imc, imc_user_base, compromisso, pontuacao_compromisso, CompromissoAtividade, ComposicaoCorporal
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
        model = CompromissoAtividade
        fields = ['id', 'descricao', 'done']


class CompromissoSerializer(serializers.ModelSerializer):
    class Meta:
        model = compromisso
        fields = ['id', 'titulo', 'data', 'hora_inicio', 'hora_fim', 'concluido']

    def create(self, validated_data):
        validated_data['id_usuario'] = self.context['request'].user
        return super().create(validated_data)


class PontuacaoCompromissoSerializer(serializers.ModelSerializer):

    data_compromisso = serializers.DateField(source='compromisso.data', format='%Y-%m-%d')
    compromisso_id = serializers.IntegerField(source='compromisso.id', read_only=True)

    class Meta:
        model = pontuacao_compromisso
        fields = [
            'id',
            'compromisso_id',
            'qtd_total_atv',
            'qtd_atv_done',
            'porcentagem',
            'criado_em',
            'data_compromisso'
        ]

class ComposicaoCorporalSerializer(serializers.ModelSerializer):
    class Meta:
        model = ComposicaoCorporal
        fields = [
            'id',
            'data_consulta',
            'gordura_percentual',
            'musculo_percentual',
            'agua_percentual',
            'gordura_visceral',
            'estimado'
        ]
        read_only_fields = ['data_consulta']

    def validate(self, data):
        user = self.context['request'].user
        today = date.today()

        registro = ComposicaoCorporal.objects.filter(
            id_usuario=user,
            data_consulta=today
        ).first()

        if registro:
            self.instance = registro

        return data

    def create(self, validated_data):
        validated_data['id_usuario'] = self.context['request'].user
        validated_data['data_consulta'] = date.today()
        return super().create(validated_data)

    def update(self, instance, validated_data):
        instance.gordura_percentual = validated_data.get('gordura_percentual', instance.gordura_percentual)
        instance.musculo_percentual = validated_data.get('musculo_percentual', instance.musculo_percentual)
        instance.agua_percentual = validated_data.get('agua_percentual', instance.agua_percentual)
        instance.gordura_visceral = validated_data.get('gordura_visceral', instance.gordura_visceral)
        instance.estimado = validated_data.get('estimado', instance.estimado)
        instance.save()
        return instance