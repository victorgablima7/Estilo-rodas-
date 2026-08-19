# WheelSwap AI | Style-Rodas (Web App)

Aplicativo web completo em **HTML5, CSS3 e JavaScript ES6+** para edição generativa e simulação realista de substituição de rodas e aros em veículos, utilizando a **API Multimodal do Google Gemini (Gemini Vision)** e motor fotorrealista em Canvas.

---

## ✨ Principais Funcionalidades

1. **Front-end Responsivo e Mobile-First**: Interface moderna com tema escuro automotivo (*Carbon Dark, Electric Cyan e Amber Racing*), compatível com qualquer celular, tablet ou desktop.
2. **Integração Direta com Gemini Vision**: Envio de imagens em base64 e prompt especializado diretamente pelo navegador via `fetch` REST API (`gemini-2.5-flash` / `gemini-1.5-flash`).
3. **Persistência de API Key**: Salva sua `GEMINI_API_KEY` com segurança no `localStorage` do navegador.
4. **Comparador Interativo Antes/Depois**: Slider interativo de alta precisão para deslizar e comparar o carro original com o resultado gerado.
5. **Presets Integrados & Upload**: Escolha fotos da sua galeria/câmera ou utilize presets de alta definição gerados em tempo real (Cupê Esportivo, SUV, Sedan, Liga Leve Aro 20, Raiada Cromada e Bronze Motorsport).
6. **Download & Compartilhamento Web**: Salve a imagem resultante em alta resolução (`.jpg`) ou compartilhe diretamente via Web Share API.
7. **Zero Dependências de Compilação**: Não requer compilação de APKs, Kivy ou Buildozer. Funciona nativamente em qualquer navegador moderno.

---

## 🚀 Como Executar

### Opção 1: Abrir Diretamente no Navegador
Basta abrir o arquivo `index.html` em qualquer navegador moderno (Chrome, Safari, Edge, Firefox, Brave).

### Opção 2: Servidor Local (Node.js ou Python)
```bash
# Com Python 3
python3 -m http.server 8080

# Com Node.js (npx)
npx serve .
```
Acesse em: `http://localhost:8080` no navegador do celular ou computador.

---

## 📁 Estrutura de Arquivos

- `index.html`: Estrutura semântica da aplicação web com seções de upload, presets, controles e comparador.
- `style.css`: Estilização completa com design system M3/Carbon, micro-interações, glassmorphism e responsividade.
- `app.js`: Lógica de gerenciamento de estado, presets procedurais em Canvas, integração REST com Gemini API, slider interativo e exportação de imagem.
- `README.md`: Instruções de uso e arquitetura da aplicação.
