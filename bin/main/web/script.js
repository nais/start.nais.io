const sendBtns = document.querySelectorAll("button")
const form = document.forms[0]
const modal = document.querySelector(".modal")
const modalCloser = document.getElementsByClassName("close-button")[0]

sendBtns.forEach(btn => {
   btn.addEventListener("click", event => {
      event.preventDefault()
      const acceptedContentType = event.target.id === "btnZip" ? "application/zip" : "application/json"
      post(form, acceptedContentType)
   })
})

document.querySelectorAll('input[type=text]').forEach(txtField => {
   txtField.addEventListener('keyup', event => {
      event.preventDefault()
      const formIsValid = form.checkValidity()
      sendBtns.forEach(btn => btn.disabled = !formIsValid)
   })
})

function computeHighlightLang(filename) {
   return filename.split('.').pop().toLowerCase();
}

function addCodeBlock(block) {
   const extension = computeHighlightLang(block.key)
   const element = document.createElement('div')
   element.classList.add('code-block')
   element.innerHTML = `<h3>${block.key}</h3><pre><code class="language-${extension}">${block.value}</code></pre>`
   return element
}

const post = (form, acceptedContentType) => {
   const formAsJson = convertToJson(form)
   makeRequest(formAsJson, acceptedContentType)
      .then(response => parse(response))
      .then(async parsedResponse => {
         if (parsedResponse.contentType === "application/zip") {
            saveBlob(parsedResponse)
         } else {

            const codeBlocks = document.getElementById('code-blocks')
            const blocks = await formatForDisplay(parsedResponse)

            blocks.map(addCodeBlock).forEach(element => codeBlocks.appendChild(element))

            hljs.highlightAll();
            toggleModal()
         }
         setErrorMsg("")
      }).catch(err => {
         setErrorMsg(`oh noes: ${err}`)
      })
}

const convertToJson = (form) => ({
   appName: form['app'].value,
   team: form['team'].value,
   platform: form['platform'].value,
   extras: Array.from(form.extras).filter(element => element.checked).map(element => element.value),
   kafkaTopics: csvToArray(form['kafkaTopics'].value)
})

const makeRequest = (form, contentType) =>
   fetch("/app", {
      method: "post",
      headers: {
         "Content-Type": "application/json",
         "Accept": contentType
      },
      body: JSON.stringify(form)
   })

const parse = async (response) => ({
   blob: await response.blob(),
   filename: response.headers.get("Content-Disposition") &&
      filenameFrom(response.headers.get("Content-Disposition")),
   contentType: response.headers.get("Content-Type")
})

const saveBlob = (parsedResponse) => {
   const blob = new Blob([parsedResponse.blob], { type: parsedResponse.contentType })
   const anchor = document.createElement("a")
   document.body.appendChild(anchor)
   anchor.style.cssText = "display: none"
   const url = window.URL.createObjectURL(blob)
   anchor.href = url
   anchor.download = parsedResponse.filename
   anchor.click();
   window.URL.revokeObjectURL(url);
   document.body.removeChild(anchor)
};

const filenameFrom = contentDispositionHeader => contentDispositionHeader.split("=")[1]

const setErrorMsg = txt => {
   const element = document.getElementById("errmsg")
   element.textContent = txt
   element.style.display = txt.trim().length === 0 ? "none" : "block"
}

const formatForDisplay = async (response) => {
   const json = JSON.parse(await response.blob.text())

   return Object.keys(json).map((key) => {
      return ({ key, value: atob(json[key]) })
   })
}

const csvToArray = (str) => str && str.trim().length !== 0 ?
   str.split(',').map((element) => element.trim()) : []

const toggleModal = () => {
   modal.classList.toggle("show-modal");
}

modalCloser.addEventListener("click", toggleModal);

window.addEventListener("click", (event) => {
   if (event.target === modal) {
      toggleModal();
   }
});
