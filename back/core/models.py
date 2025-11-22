from django.db import models
from django.contrib.auth.models import User


class ModelBase(models.Model):
    id = models.AutoField(db_column='id', primary_key=True)

    id_usuario = models.ForeignKey(
        User,
        db_column='id_usuario',
        on_delete=models.CASCADE,
        related_name='%(class)s_set'
    )

    class Meta:
        abstract = True

class RegistroCorporal(ModelBase):  # Antigo IMC
    data_consulta = models.DateField(db_column='data_consulta')
    peso = models.FloatField(db_column='peso', null=False)
    altura = models.FloatField(db_column='altura', null=False)
    imc_res = models.FloatField(db_column='imc_res', null=True, blank=True)
    classificacao = models.CharField(db_column='classificacao', max_length=30, null=True, blank=True)

    class Meta:
        db_table = 'registro_corporal'
        ordering = ['id']


class PerfilUsuario(ModelBase):
    id_usuario = models.OneToOneField(
        User,
        db_column='id_usuario',
        on_delete=models.CASCADE,
        related_name='perfil'
    )

    nome = models.CharField(db_column='nome', max_length=100, null=False)
    sexo = models.CharField(db_column='sexo', max_length=10, null=False)
    idade = models.IntegerField(db_column='idade', null=False)
    objetivo = models.CharField(db_column='objetivo', max_length=200, null=False)

    restricoes_alimentares = models.TextField(
        db_column='restricoes_alimentares',
        null=True,
        blank=True,
        help_text="Ex: Intolerância à lactose, Vegano"
    )

    observacao_saude = models.TextField(
        db_column='observacao_saude',
        null=True,
        blank=True,
        help_text="Ex: Asma, problema no joelho, hérnia, limitação para exercícios"
    )

    class Meta:
        db_table = 'perfil_usuario'

    def __str__(self):
        return f"Perfil de {self.nome}"

class Compromisso(ModelBase):
    titulo = models.CharField(db_column='titulo', max_length=200, null=False)
    data = models.DateField(db_column='data', null=False)
    hora_inicio = models.TimeField(db_column='hora_inicio', null=False)
    hora_fim = models.TimeField(db_column='hora_fim', null=False)
    concluido = models.BooleanField(db_column='concluido', default=False)

    class Meta:
        db_table = 'compromisso'
        unique_together = (('id_usuario', 'data', 'hora_inicio'),)
        ordering = ['-data', 'hora_inicio']

    def __str__(self):
        return f"{self.titulo} - {self.data} ({self.hora_inicio} às {self.hora_fim})"

class CompromissoAtividade(models.Model):
    id = models.AutoField(db_column='id', primary_key=True)
    descricao = models.TextField(db_column='descricao')
    done = models.BooleanField(db_column='done', default=False)
    compromisso = models.ForeignKey('Compromisso', db_column='id_compromisso', on_delete=models.CASCADE,
                                    related_name='atividades')

    class Meta:
        db_table = 'compromisso_atividade'
        ordering = ['id']

    def __str__(self):
        status = "✔️" if self.done else "❌"
        return f"{status} {self.descricao}"


class PontuacaoCompromisso(models.Model):
    id = models.AutoField(db_column='id', primary_key=True)
    compromisso = models.OneToOneField('Compromisso', on_delete=models.CASCADE, related_name='pontuacao')
    qtd_total_atv = models.PositiveIntegerField(db_column='qtd_total_atv', default=0)
    qtd_atv_done = models.PositiveIntegerField(db_column='qtd_atv_done', default=0)
    porcentagem = models.FloatField(db_column='porcentagem', default=0)
    criado_em = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'pontuacao_compromisso'
        ordering = ['id']

    def __str__(self):
        return f"Pontuação - Compromisso #{self.compromisso.id}: {self.porcentagem:.2f}%"

class ComposicaoCorporal(ModelBase):
    data_consulta = models.DateField(db_column='data_consulta', auto_now_add=True)
    gordura_percentual = models.FloatField(db_column='gordura_percentual', null=True, blank=True)
    musculo_percentual = models.FloatField(db_column='musculo_percentual', null=True, blank=True)
    agua_percentual = models.FloatField(db_column='agua_percentual', null=True, blank=True)
    gordura_visceral = models.IntegerField(db_column='gordura_visceral', null=True, blank=True)
    estimado = models.BooleanField(db_column='estimado', default=False)

    class Meta:
        db_table = 'composicao_corporal'
        ordering = ['-data_consulta']

class Dieta(ModelBase):
    id_usuario = models.ForeignKey(
        User,
        db_column='id_usuario',
        on_delete=models.CASCADE,
        related_name='dietas'
    )
    data_criacao = models.DateTimeField(db_column='data_criacao', auto_now_add=True)
    plano_alimentar = models.JSONField(db_column='plano_alimentar', null=False)

    class Meta:
        db_table = 'dieta'
        ordering = ['-data_criacao']

    def __str__(self):
        return f"Dieta {self.id} - {self.data_criacao}"

# --- NOVO MODEL ADICIONADO ---
class HistoricoExercicio(ModelBase):
    nome_exercicio = models.CharField(db_column='nome_exercicio', max_length=150, null=False)
    duracao_segundos = models.PositiveIntegerField(db_column='duracao_segundos', null=False)
    calorias_queimadas = models.PositiveIntegerField(db_column='calorias_queimadas', null=False)
    data_treino = models.DateTimeField(db_column='data_treino', null=False)

    class Meta:
        db_table = 'historico_exercicio'
        ordering = ['-data_treino']

    def __str__(self):
        return f"{self.nome_exercicio} ({self.calorias_queimadas}kcal)"