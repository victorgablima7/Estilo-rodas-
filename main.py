"""
Aplicativo Kivy - WheelSwap AI (Edição Generativa de Rodas com Gemini API)
Interface gráfica moderna em Python/Kivy com suporte a seleção de arquivos,
chamada assíncrona da API do Gemini e exibição do resultado.
"""

import os
import threading
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.gridlayout import GridLayout
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.button import Button
from kivy.uix.image import Image as KivyImage
from kivy.uix.progressbar import ProgressBar
from kivy.uix.popup import Popup
from kivy.uix.filechooser import FileChooserIconView
from kivy.uix.scrollview import ScrollView
from kivy.clock import Clock
from kivy.core.window import Window
from kivy.graphics import Color, RoundedRectangle

from gemini_service import GeminiWheelService

# Configuração da Janela
Window.clearcolor = (0.05, 0.07, 0.11, 1.0)
Window.size = (480, 820)

class WheelSwapApp(App):
    def build(self):
        self.title = "WheelSwap AI - Troca Generativa de Rodas"
        self.caminho_carro = ""
        self.caminho_roda = ""
        self.resultado_arquivo = "resultado_troca.png"

        # Layout Principal
        root = ScrollView(size_hint=(1, 1), do_scroll_x=False)
        layout = BoxLayout(orientation='vertical', padding=20, spacing=15, size_hint_y=None)
        layout.bind(minimum_height=layout.setter('height'))

        # Cabeçalho / Título
        lbl_titulo = Label(
            text="[b][color=00E5FF]WHEEL[/color]SWAP [color=FF9100]AI[/color][/b]",
            markup=True,
            font_size="24sp",
            size_hint_y=None,
            height=40
        )
        layout.add_widget(lbl_titulo)

        lbl_subtitulo = Label(
            text="Edição Generativa de Rodas Automotivas com Gemini Vision",
            font_size="13sp",
            color=(0.6, 0.65, 0.75, 1),
            size_hint_y=None,
            height=20
        )
        layout.add_widget(lbl_subtitulo)

        # 1. Campo de inserção da GEMINI_API_KEY
        lbl_key = Label(
            text="[b]Chave da API Gemini (GEMINI_API_KEY):[/b]",
            markup=True,
            font_size="14sp",
            halign="left",
            size_hint_y=None,
            height=24
        )
        lbl_key.bind(size=lbl_key.setter('text_size'))
        layout.add_widget(lbl_key)

        self.input_api_key = TextInput(
            text=os.environ.get("GEMINI_API_KEY", ""),
            hint_text="Cole sua GEMINI_API_KEY aqui...",
            password=True,
            multiline=False,
            size_hint_y=None,
            height=44,
            background_color=(0.1, 0.14, 0.22, 1),
            foreground_color=(1, 1, 1, 1),
            padding=[10, 12, 10, 12]
        )
        layout.add_widget(self.input_api_key)

        # 2. Seleção de Imagens (Grid com Carro e Roda)
        grid_selecao = GridLayout(cols=2, spacing=10, size_hint_y=None, height=180)

        # Coluna Carro
        box_carro = BoxLayout(orientation='vertical', spacing=5)
        self.btn_carro = Button(
            text="Selecionar\nFoto do Carro",
            font_size="13sp",
            background_color=(0.0, 0.89, 1.0, 1),
            color=(0.05, 0.07, 0.11, 1),
            bold=True
        )
        self.btn_carro.bind(on_release=lambda x: self.abrir_seletor("carro"))
        self.img_preview_carro = KivyImage(source="", size_hint_y=0.6)
        self.lbl_status_carro = Label(text="Nenhum selecionado", font_size="11sp", size_hint_y=0.2, color=(0.7, 0.7, 0.7, 1))
        box_carro.add_widget(self.btn_carro)
        box_carro.add_widget(self.img_preview_carro)
        box_carro.add_widget(self.lbl_status_carro)
        grid_selecao.add_widget(box_carro)

        # Coluna Roda
        box_roda = BoxLayout(orientation='vertical', spacing=5)
        self.btn_roda = Button(
            text="Selecionar\nFoto da Roda",
            font_size="13sp",
            background_color=(1.0, 0.57, 0.0, 1),
            color=(0.05, 0.07, 0.11, 1),
            bold=True
        )
        self.btn_roda.bind(on_release=lambda x: self.abrir_seletor("roda"))
        self.img_preview_roda = KivyImage(source="", size_hint_y=0.6)
        self.lbl_status_roda = Label(text="Nenhum selecionado", font_size="11sp", size_hint_y=0.2, color=(0.7, 0.7, 0.7, 1))
        box_roda.add_widget(self.btn_roda)
        box_roda.add_widget(self.img_preview_roda)
        box_roda.add_widget(self.lbl_status_roda)
        grid_selecao.add_widget(box_roda)

        layout.add_widget(grid_selecao)

        # 3. Botão Processar Troca
        self.btn_processar = Button(
            text="PROCESSAR TROCA DE RODAS",
            font_size="15sp",
            bold=True,
            size_hint_y=None,
            height=50,
            background_color=(0.0, 0.9, 0.46, 1),
            color=(0.05, 0.07, 0.11, 1)
        )
        self.btn_processar.bind(on_release=self.iniciar_processamento)
        layout.add_widget(self.btn_processar)

        # 4. Indicador de Carregamento e Mensagens de Status
        self.progress_bar = ProgressBar(max=100, size_hint_y=None, height=15, opacity=0)
        layout.add_widget(self.progress_bar)

        self.lbl_status_geral = Label(
            text="Selecione as duas fotos e clique em Processar",
            font_size="12sp",
            color=(0.7, 0.75, 0.85, 1),
            size_hint_y=None,
            height=24
        )
        layout.add_widget(self.lbl_status_geral)

        # 5. Área para Exibir a Imagem Gerada Retornada
        lbl_resultado_titulo = Label(
            text="[b]Resultado da Edição Generativa:[/b]",
            markup=True,
            font_size="14sp",
            halign="left",
            size_hint_y=None,
            height=24
        )
        lbl_resultado_titulo.bind(size=lbl_resultado_titulo.setter('text_size'))
        layout.add_widget(lbl_resultado_titulo)

        self.img_resultado = KivyImage(
            source="",
            size_hint_y=None,
            height=280,
            allow_stretch=True,
            keep_ratio=True
        )
        layout.add_widget(self.img_resultado)

        root.add_widget(layout)
        return root

    def abrir_seletor(self, tipo):
        """Abre modal com FileChooser para escolher foto do carro ou roda"""
        box = BoxLayout(orientation='vertical', spacing=10, padding=10)
        filechooser = FileChooserIconView(filters=['*.png', '*.jpg', '*.jpeg', '*.webp'], path=os.path.expanduser("~"))
        box.add_widget(filechooser)

        btn_confirmar = Button(text="Confirmar Seleção", size_hint_y=None, height=45)
        box.add_widget(btn_confirmar)

        popup = Popup(
            title=f"Escolha a Foto do {'Carro' if tipo == 'carro' else 'Roda'}",
            content=box,
            size_hint=(0.9, 0.9)
        )

        def selecionar(instance):
            if filechooser.selection:
                arquivo = filechooser.selection[0]
                if tipo == "carro":
                    self.caminho_carro = arquivo
                    self.lbl_status_carro.text = os.path.basename(arquivo)
                    self.img_preview_carro.source = arquivo
                    self.img_preview_carro.reload()
                else:
                    self.caminho_roda = arquivo
                    self.lbl_status_roda.text = os.path.basename(arquivo)
                    self.img_preview_roda.source = arquivo
                    self.img_preview_roda.reload()
            popup.dismiss()

        btn_confirmar.bind(on_release=selecionar)
        popup.open()

    def iniciar_processamento(self, instance):
        api_key = self.input_api_key.text.strip()
        if not api_key:
            self.lbl_status_geral.text = "[Erro] Informe sua chave da API do Gemini!"
            return

        if not self.caminho_carro or not self.caminho_roda:
            self.lbl_status_geral.text = "[Erro] Selecione a foto do Carro e da Roda!"
            return

        # Ativar carregamento
        self.btn_processar.disabled = True
        self.progress_bar.opacity = 1
        self.progress_bar.value = 30
        self.lbl_status_geral.text = "Conectando ao Gemini AI e processando imagens..."

        # Disparo assíncrono em Thread
        thread = threading.Thread(target=self._executar_gemini, args=(api_key,))
        thread.daemon = True
        thread.start()

    def _executar_gemini(self, api_key):
        try:
            service = GeminiWheelService(api_key=api_key)
            Clock.schedule_once(lambda dt: self._atualizar_progresso(60, "Aplicando encaixe e iluminação realista..."), 0)
            
            caminho_final = service.processar_troca_de_rodas(
                caminho_carro=self.caminho_carro,
                caminho_roda=self.caminho_roda,
                caminho_saida=self.resultado_arquivo
            )
            Clock.schedule_once(lambda dt: self._finalizar_sucesso(caminho_final), 0)
        except Exception as e:
            Clock.schedule_once(lambda dt: self._finalizar_erro(str(e)), 0)

    def _atualizar_progresso(self, valor, mensagem):
        self.progress_bar.value = valor
        self.lbl_status_geral.text = mensagem

    def _finalizar_sucesso(self, caminho_imagem):
        self.progress_bar.value = 100
        self.progress_bar.opacity = 0
        self.btn_processar.disabled = False
        self.lbl_status_geral.text = "[Sucesso] Rodas trocadas com sucesso!"
        self.img_resultado.source = caminho_imagem
        self.img_resultado.reload()

    def _finalizar_erro(self, erro_msg):
        self.progress_bar.opacity = 0
        self.btn_processar.disabled = False
        self.lbl_status_geral.text = f"[Erro na API] {erro_msg}"

if __name__ == '__main__':
    WheelSwapApp().run()
