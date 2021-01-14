document.getElementById("send").addEventListener("click", event => {
   const form = document.forms[0]
   const missingValues = validateAllValuesPresent(form)
   if (missingValues) {
      setErrorMsg("Alle verdier må fylles ut")
   } else {
      post(form)
   }
   event.preventDefault()
})

const post = form => {
   const formData = {
      appName: form['app'].value,
      team: form['team'].value,
      image: form['image'].value,
      platform: form['platform'].value
   }
   fetch("/app", {
      method: "post",
      headers: {
         "Content-Type": "application/json"
      },
      body: JSON.stringify(formData)
   }).then(async response => ({
      blob: await response.blob(),
      filename: filenameFrom(response.headers.get("Content-Disposition")),
      contentType: response.headers.get("Content-Type")
   })).then(parsedResponse => {
      const newBlob = new Blob([parsedResponse.blob], { type: parsedResponse.contentType })
      saveBlob(newBlob, parsedResponse.filename)
      setErrorMsg("")
   }).catch(err => {
      setErrorMsg(`oh noes: ${err}`)
   })
}

const validateAllValuesPresent = form =>
   Array.from(form.elements)
      .filter(element => element.id !== "send")
      .some(element => !element.value || element.value.trim().length === 0)

const saveBlob = (blob, fileName) => {
   const a = document.createElement("a")
   document.body.appendChild(a)
   a.style.cssText = "display: none"
   const url = window.URL.createObjectURL(blob)
   a.href = url
   a.download = fileName
   a.click();
   window.URL.revokeObjectURL(url);
   document.body.removeChild(a)
};

const filenameFrom  = contentDispositionHeader => contentDispositionHeader.split("=")[1]

const setErrorMsg = txt => {
   const element = document.getElementById("errmsg")
   element.textContent = txt
   element.style.display = txt.trim().length === 0 ? "none" : "block"
}
