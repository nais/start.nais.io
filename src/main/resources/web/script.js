const sendBtn = document.getElementById("send")
const form = document.forms[0]

sendBtn.addEventListener("click", event => {
   event.preventDefault()
   post(form)
})

document.querySelectorAll('input[type=text]').forEach(txtField => {
   txtField.addEventListener('keyup', event => {
      event.preventDefault()
      sendBtn.disabled = !form.checkValidity()
   })
})

const post = form => {
   const formAsJson = convertToJson(form)
   makeRequest(formAsJson, "application/zip")
      .then(response => parse(response))
      .then(parsedResponse => {
         saveBlob(parsedResponse)
         setErrorMsg("")
      }).catch(err => {
      setErrorMsg(`oh noes: ${err}`)
   })
}

const convertToJson = (form) => ({
   appName: form['app'].value,
   team: form['team'].value,
   platform: form['platform'].value,
   extras: Array.from(form.extras).filter(element => element.checked).map(element => element.value)
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
   filename: filenameFrom(response.headers.get("Content-Disposition")),
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

const filenameFrom  = contentDispositionHeader => contentDispositionHeader.split("=")[1]

const setErrorMsg = txt => {
   const element = document.getElementById("errmsg")
   element.textContent = txt
   element.style.display = txt.trim().length === 0 ? "none" : "block"
}
