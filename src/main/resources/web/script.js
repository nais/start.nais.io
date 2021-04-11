const sendBtns = document.querySelectorAll("button")
const form = document.forms[0]
const modal = document.getElementById("myModal");
const closer = document.getElementsByClassName("close")[0]

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

const post = (form, acceptedContentType) => {
   const formAsJson = convertToJson(form)
   makeRequest(formAsJson, acceptedContentType)
      .then(response => parse(response))
      .then(async parsedResponse => {
         if (parsedResponse.contentType === "application/zip") {
            saveBlob(parsedResponse)
         } else {
            document.getElementById("modaltxt").textContent =
               await formatForDisplay(parsedResponse)
            modal.style.display = "block"
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

const filenameFrom  = contentDispositionHeader => contentDispositionHeader.split("=")[1]

const setErrorMsg = txt => {
   const element = document.getElementById("errmsg")
   element.textContent = txt
   element.style.display = txt.trim().length === 0 ? "none" : "block"
}

const formatForDisplay = async (response) => {
   const json = JSON.parse(await response.blob.text())
   return Object.keys(json).map((key) =>
      `${key}:\n-------------------------\n${atob(json[key])}`).join("\n\n")
}

const csvToArray = (str) => str && str.trim().length !== 0 ?
   str.split(',').map((element) => element.trim())  : []

closer.onclick = () => {
   modal.style.display = "none"
}

window.onclick = (event) => {
   if (event.target === modal) {
      modal.style.display = "none";
   }
}

